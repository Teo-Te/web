package com.webapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class Controller1 {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodosRepository todosRepository;

    @Autowired
    private Validate validate;

    @Autowired
    private RatingRepository ratingRepository;

    //Generic method usage just for the sake of the requirement
    public <T> void addAttributeToModel(Model model, String attributeName, T attributeValue) {
    model.addAttribute(attributeName, attributeValue);
    }

    //Route mapping for the index page
    @GetMapping("/")
    public String index(Model model) {
        //Getting the reviews to be displayed in the lading page
        List<Rating> ratings = ratingRepository.findAll();
        //Adding them to the model
        addAttributeToModel(model, "ratings", ratings.reversed());
        return "index";
    }

    //Route mapping for the login page
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    //Route mapping for the register page
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    //Route mapping for the dashboard page
    @GetMapping("/dashboard/{username}")
    public String dashboard(@PathVariable String username, Model model, HttpSession session) {
        //Check if the user actually logged in
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }
        //Get the user from the database and add to the model
        User user = userRepository.findByUsername(username);
        addAttributeToModel(model, "user", user);

        //Get the todos for the user, store them in a LinkedList for better efficiency
        //(LinkedList is faster than ArrayList for adding and removing elements)
        //No need to resize the internal array, no need to shift elements as per the controary of ArrayList
        //Anddd it is a project requirement soo... We need to use it
        LinkedList<Todos> todos = new LinkedList<>(todosRepository.findByUsername(username));
        //Collections is a utility class that provides static methods for sorting and searching
        Collections.sort(todos, new TodosComparator());
        
        //Add the todos to the model
        addAttributeToModel(model, "todos", todos);

        return "dashboard/index";
    }

    //Mapping Post for the rating
    @PostMapping("/rate")
    public String rate(@RequestParam("rating") int rating, RedirectAttributes redirectAttributes) {
    //Set the text based on the rating
    String text = "";
    if (rating == 1) {
        text = "Oh no! We were this bad?";
    } else if (rating == 2) {
        text = "I guess we have a lot to improve on.";
    } else if (rating == 3) {
        text = "Not bad, but we can do better.";
    } else if (rating == 4) {
        text = "Great! But we can still improve.";
    } else if (rating == 5) {
        text = "Awesome! Thanks for the good rating teacher!";
    }
    //Save the rating to the database
    //Landing page rating is anonymous and has no description
    String username = "Unknown";
    String description = "No description";
    ratingRepository.save(new Rating(rating, description, username));

    //Add the rating text to the model
    redirectAttributes.addFlashAttribute("ratingText", text);

    //Redirect to the index page
    return "redirect:/#starRating";
    }

    //Mapping Post request for the register
    @PostMapping("/registerUser")
    public String registerUser(@RequestParam String username, @RequestParam String name, @RequestParam String email, 
    @RequestParam String password, Model model) {
        //Validate the username, name, email, and password
        if (!validate.isValidUsername(username)) {
            model.addAttribute("error", "Invalid username");
            return "register";
        } else if (!validate.isValidName(name)) {
            model.addAttribute("error", "Invalid name");
            return "register";
        } else if (!validate.isValidEmail(email)) {
            model.addAttribute("error", "Invalid email");
            return "register";
        } else if (!validate.isValidPassword(password)) {
            model.addAttribute("error", "Invalid password");
            return "register";
        }
        //Check if the username or email already exists in the database
        if (userExists(username, email)) {
            model.addAttribute("error", "Username or email already exists");
            return "register";
        }

        //Create a new user object
        User user = new User(username, name, email, password);

        //Save the user object to the database
        userRepository.save(user);

        //Redirect to the login page
        return "login";
    }

    //Does a username or email already exist in the database?
    private boolean userExists(String username, String email) {
        //Check if the username or email exists in the database
        if (userRepository.findByUsername(username) != null || userRepository.findByEmail(email) != null) {
            return true;
        } else {
            return false;
        }
    }

    //Mapping Post request for the login
    @PostMapping("/loginUser")
    public String loginUser(@RequestParam String username, @RequestParam String password, Model model, HttpSession session) {
        //Check if the username exists in the database
        User user = userRepository.findByUsername(username);
        
        //If the username does not exist, return an error
        if (user == null) {
            model.addAttribute("error", "Username does not exist");
            return "login";
        }

        //If the password is incorrect, return an error
        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "Password is incorrect");
            return "login";
        }

        session.setAttribute("username", username);
        //Redirect to the dashboard page
        return "redirect:/dashboard/" + username;
    }

    //Mapping Post request for the addTodo
    @PostMapping("/addTodo")
    public String addTodo(@RequestParam String username, @RequestParam String description, @RequestParam String date, RedirectAttributes redirectAttributes) {
        //Validate the date
        if (!validate.isValidDate(date)) {
            redirectAttributes.addFlashAttribute("error", "Invalid date");
            return "redirect:/dashboard/" + username + "#dashboard";
        }
        //Create a new todo object
        Todos todos = new Todos(username, description, date);

        try {
            //Save the todo object to the database
            todosRepository.save(todos);
        } catch (Exception e) {
            //Otherwise display an error message
            redirectAttributes.addFlashAttribute("error", "An error occurred");
            return "redirect:/dashboard/" + username + "#dashboard";
        }
        //Display a success message
        redirectAttributes.addFlashAttribute("success", "Todo added successfully");
        //Redirect to the dashboard page
        return "redirect:/dashboard/" + username + "#tasks";
    }

    //Mapping Post request for the deleteTodo
    @PostMapping("/deleteTodo")
    public String deleteTodo(@RequestParam int id, @RequestParam String username, RedirectAttributes model) {
        try {
            //Delete the todo from the database
            todosRepository.deleteById(id);
        } catch (Exception e) {
            //Otherwise display an error message
            model.addFlashAttribute("error", "An error occurred");
            return "redirect:/dashboard/" + username + "#tasks";
        }
        //Display a success message
        model.addFlashAttribute("success", "Todo deleted successfully");
        //Redirect to the dashboard page
        return "redirect:/dashboard/" + username + "#tasks";
    }

    //Mapping Post request fot the updateUser
    @PostMapping("/updateUser")
    public String updateUser(@RequestParam String defaultName, @RequestParam String defaultEmail, @RequestParam String defaultUsername, @RequestParam String defaultPassword, @RequestParam String username, @RequestParam String name, @RequestParam String email, @RequestParam String password, RedirectAttributes model) {

        //Check if the fields are empty so we set the default values
        if (username.equals("")) {
            username = defaultUsername;
        }
        if (name.equals("")) {
            name = defaultName;
        }
        if (email.equals("")) {
            email = defaultEmail;
        }
        
        //Check if the default password and the password are the same so the changes are correct
        if (!password.equals(defaultPassword)) {
            model.addFlashAttribute("error", "Password is incorrect");
            return "redirect:/dashboard/" + defaultUsername + "#account";
        }

        //Validate the username, name, email, and password
        if (!validate.isValidUsername(username)) {
            model.addFlashAttribute("error", "Invalid username");
            return "redirect:/dashboard/" + username + "#account";
        } else if (!validate.isValidName(name)) {
            model.addFlashAttribute("error", "Invalid name");
            return "redirect:/dashboard/" + username + "#account";
        } else if (!validate.isValidEmail(email)) {
            model.addFlashAttribute("error", "Invalid email");
            return "redirect:/dashboard/" + username + "#account";
        } 
    
        User existingUser = userRepository.findByUsername(defaultUsername);
        //Check if the username already exists in the database besides the current user
        if (userExists(username, email) && !existingUser.getEmail().equals(email) && !existingUser.getUsername().equals(username)) {
            model.addFlashAttribute("error", "Username or email already taken");
            return "redirect:/dashboard/" + username + "#account";
        }

        //Update the user object
        existingUser.setUsername(username);
        existingUser.setName(name);
        existingUser.setEmail(email);
        
        //Save the user object to the database
        userRepository.save(existingUser);

        //In case the username changes, update the todos
        ArrayList<Todos> todos = todosRepository.findByUsername(defaultUsername);
        for (Todos todo : todos) {
            todo.setUsername(username);
            todosRepository.save(todo);
        }

        //Display Success message
        model.addFlashAttribute("success", "User updated successfully");

        //Redirect to the dashboard page
        return "redirect:/dashboard/" + username + "#account";
    }

    //Mapping Post request for the deleteAccount dialog
    @PostMapping("/deleteAccountDialog")
    public String deleteAccount(@RequestParam String username, RedirectAttributes model) {
        //Are you sure you want to delete the account? Dialog
        model.addFlashAttribute("warning", "Are you sure you want to delete your account? This action cannot be undone.");

        //Redirect to the index page
        return "redirect:/dashboard/" + username + "#account";
    }

    //Mapping Post request for the deleteAccount
    @PostMapping("/deleteAccount")
    public String deleteAccount(@RequestParam String username) {
        //Delete the user from the database
        User user = userRepository.findByUsername(username);
        userRepository.delete(user);

        //Delete the todos from the database
        ArrayList<Todos> todos = todosRepository.findByUsername(username);
        for (Todos todo : todos) {
            todosRepository.delete(todo);
        }

        //Redirect to the index page
        return "redirect:/";
    }

    //Mapping Post request for the changePasswordDialog
    @PostMapping("/changePasswordDialog")
    public String changePasswordDialog(@RequestParam String username, RedirectAttributes model) {
        //Change password dialog
        model.addFlashAttribute("info", "Please insert your new password below along with your current password.");

        //Redirect to the index page
        return "redirect:/dashboard/" + username + "#account";
    }

    //Mapping Post request for the changePassword
    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String username, @RequestParam String defaultPassword, @RequestParam String password, @RequestParam String confirmPassword, @RequestParam String oldPassword, RedirectAttributes model) {
        //Check if the default password and the password are the same so the changes are correct
        if (!defaultPassword.equals(oldPassword)) {
            model.addFlashAttribute("error", "Current password is incorrect");
            return "redirect:/dashboard/" + username + "#account";
        }

        //Validate the password
        if (!validate.isValidPassword(password)) {
            model.addFlashAttribute("error", "Invalid password");
            return "redirect:/dashboard/" + username + "#account";
        }

        //Check if the password and the confirm password are the same
        if (!password.equals(confirmPassword)) {
            model.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/dashboard/" + username + "#account";
        }

        //Update the user object
        User existingUser = userRepository.findByUsername(username);
        existingUser.setPassword(password);

        //Save the user object to the database
        userRepository.save(existingUser);

        //Display Success message
        model.addFlashAttribute("success", "Password changed successfully");

        //Redirect to the dashboard page
        return "redirect:/dashboard/" + username + "#account";
    }

    //Mapping post request for the rating
    @PostMapping("/rateExperience")
    public String rateExperience(@RequestParam String username, @RequestParam int rating, @RequestParam String description, RedirectAttributes model) {

        if (description.equals("")) {
            description = "No description";
        }

        if (rating == 0 ) {
            model.addFlashAttribute("error", "Invalid rating");
            return "redirect:/dashboard/" + username + "#rating";
        }

        //Create a new rating object
        Rating ratingObject = new Rating(rating, description, username);

        //Save the rating object to the database
        ratingRepository.save(ratingObject);

        //Display Success message
        model.addFlashAttribute("success", "Thank you for rating us!");

        //Redirect to the dashboard page
        return "redirect:/dashboard/" + username + "#rating";
    }

    //Mapping get request for the logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        //Invalidate the session
        session.invalidate();

        //Redirect to the index page
        return "redirect:/";
    }

}
