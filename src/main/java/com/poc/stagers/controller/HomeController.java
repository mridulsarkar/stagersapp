package com.poc.stagers.controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.poc.stagers.exception.CustomException;
import com.poc.stagers.jwt.JwtTokenProvider;
import com.poc.stagers.models.Event;
import com.poc.stagers.models.EventStatus;
import com.poc.stagers.models.User;
import com.poc.stagers.repositories.EventRepository;
import com.poc.stagers.repositories.EventStatusRepository;
import com.poc.stagers.service.SequenceGeneratorService;
import com.poc.stagers.service.UserServiceImpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class HomeController
{
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;
    @Autowired
    private EventRepository eventRepo;
    @Autowired
    private EventStatusRepository eventStatusRepo;
    @Autowired
	AuthenticationManager authenticationManager;

    private static final Logger logger = LogManager.getLogger(HomeController.class);
    
    @RequestMapping(value = { "/", "/home" }, method = { RequestMethod.GET })
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("index");
        return modelAndView;
    }

    @RequestMapping(value = { "/login" }, method = { RequestMethod.GET })
    public ModelAndView loginPage() {
        ModelAndView modelAndView = new ModelAndView("login");
        User user = new User();
        modelAndView.addObject("user", user);
        return modelAndView;   
    }
    
    @RequestMapping(value = { "/login" }, method = { RequestMethod.POST })
    public ModelAndView loginAuthentication(HttpServletResponse response, @Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView("login");
        User userExists = null;

        try {
            userExists = userService.findByUsername(user.getUsername());
            if (null != userExists) { //if user exists then only proceed for jwt token authentication
                logger.info("Authenticating User: " + user.getUsername());
                // Set jwt token in cookie
                String jwtToken = userService.login(user.getUsername(), user.getPassword());
                response.addCookie(JwtTokenProvider.generateCookie(jwtToken));

                modelAndView.addObject("successMessage", "User logged-in successfully");
                modelAndView.addObject("user", user);
                modelAndView.addObject("token", jwtToken);
                modelAndView.setViewName("dashboard"); // Dashboard is the landing page
            }
        } catch (CustomException exp) { // Authentication failure
            modelAndView.addObject("errorMsg", "UserName or Password is Invalid");
        } catch (Exception exp) { // Any other exception
            logger.error(exp.getLocalizedMessage());
            modelAndView.addObject("errorMsg", exp.getLocalizedMessage());
            return modelAndView;
        }

        return modelAndView;
    }
    
    @RequestMapping(value = { "/signup" }, method = { RequestMethod.GET })
    public ModelAndView signup() {
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("signup");
        return modelAndView;
    }
    
    @RequestMapping(value = { "/signup" }, method = { RequestMethod.POST })
    public ModelAndView createNewUser(HttpServletResponse response, @Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView("dashboard"); //dashboard is the default landing page
        User userExists = null;

        try {
            userExists = userService.findByUsername(user.getUsername());
        } catch (Exception exp) {
            logger.error(exp.getLocalizedMessage());
            modelAndView.addObject("errorMsg", exp.getLocalizedMessage());
            modelAndView.setViewName("signup");
            return modelAndView;
        }

        // If an existing user send them back to signup page and display the error message
        if (null != userExists || bindingResult.hasErrors()) {
            modelAndView.addObject("errorMsg", "There is already a user registered with the username " + user.getUsername() + "!");
            modelAndView.setViewName("signup");
        } else {
            // Create New user
            logger.info("Creating User: " + user.getUsername());
            user.setId(this.sequenceGeneratorService.generateSequence("users_sequence"));
            // Set jwt token in cookie
            String jwtToken = userService.signup(user);
            response.addCookie(JwtTokenProvider.generateCookie(jwtToken));
            // Set the reqd objects into the modelandview
            modelAndView.addObject("successMessage", "User has been registered successfully");
            modelAndView.addObject("user", user);
        }
        return modelAndView;
    }
    
    @RequestMapping(value = { "/dashboard" }, method = { RequestMethod.GET })
    public ModelAndView dashboard() {
        ModelAndView modelAndView = new ModelAndView();
        User user = loggedInUser();
        modelAndView.addObject("currentUser", user);
        modelAndView.addObject("fullName", user.getFirstName() + " " + user.getLastName());
        modelAndView.setViewName("dashboard");
        return modelAndView;
    }
    
    @RequestMapping(value = { "/createevent" }, method = { RequestMethod.GET })
    public ModelAndView createEventPage() {
        ModelAndView modelAndView = new ModelAndView();
        User user = loggedInUser();
        if (user != null) {
            modelAndView.addObject("currentUser", user);
            modelAndView.addObject("fullName", user.getFirstName() + " " + user.getLastName());
            modelAndView.setViewName("/createevent");
        } else {
            modelAndView.setViewName("/index");
        }
        return modelAndView;
    }
    
    @RequestMapping(value = { "/createevent" }, method = { RequestMethod.POST })
    public ModelAndView createEvent(HttpServletRequest request, 
                                    @RequestParam(value = "eventId", required = true) String eventId, 
                                    @RequestParam(value = "status", required = true) String status, 
                                    @RequestParam(value = "title", required = true) String title, 
                                    @RequestParam(value = "amount", required = true) String amount, 
                                    @RequestParam(value = "description", required = true) String description, 
                                    @RequestParam(value = "deadline", required = true) String deadline) {

        logger.info("Creating Event: " + eventId);

        ModelAndView modelAndView = new ModelAndView();
        EventStatus eventStatus = this.eventStatusRepo.findByEventStatus(status);
        User user = loggedInUser();
        try {
            if (user == null) {
                modelAndView.addObject("errorMsg", "No Logged in User, cannot create event");
                modelAndView.setViewName("index");
            } else {
                DateFormat format = new SimpleDateFormat("yyyy-mm-dd");
                Date respose = format.parse(deadline);
                Event event = new Event(eventId, title, description, new BigDecimal(amount), 
                                                                eventStatus, user, respose);
                event.setId(sequenceGeneratorService.generateSequence("event_sequence"));
                eventRepo.save(event);
                modelAndView.addObject("currentUser", user);
                modelAndView.addObject("currentEvent", event);
                modelAndView.addObject("successMsg", "Event " + eventId + " created successfully");
                modelAndView.setViewName("dashboard");
            }
        } catch (Exception exp) {
            modelAndView.addObject("errorMsg", "Cannot create event, " + exp.getMessage());
            modelAndView.setViewName("redirect:/createevent");
        }
        return modelAndView;
    }
    
    @RequestMapping(value = { "/eventdetails" }, method = { RequestMethod.GET })
    public ModelAndView eventDetails(HttpServletRequest request) {
        String id = request.getParameter("eventid");
        String referer = request.getHeader("Referer");
        ModelAndView modelAndView = new ModelAndView();
        User eventCreator = loggedInUser();
        Event event = eventRepo.findByCreatorAndId(eventCreator, Long.parseLong(id));
        
        if (event == null) {
            modelAndView.addObject("errorMsg", "Event Id " + id + " could not be found!");
            modelAndView.setViewName("openeventslist");
        } else {
            final User user = this.loggedInUser();
            modelAndView.addObject("eventDetail", event);
            modelAndView.addObject("currentUser", user);
            modelAndView.addObject("fullName", user.getFirstName() + " " + user.getLastName());
            modelAndView.addObject("referer", referer);
            modelAndView.setViewName("eventdetails");
        }
        return modelAndView;
    }
    
    @RequestMapping(value = { "/openeventslist" }, method = { RequestMethod.GET })
    public ModelAndView openEventsList(final Model model) {
        ModelAndView modelAndView = new ModelAndView("redirect:/openeventslist");
        User eventCreator = loggedInUser();
        EventStatus eventStatus = this.eventStatusRepo.findByEventStatus(EventStatus.EventStatusEnum.CONCLUDED.toString());
        List<Event> allEvents = this.eventRepo.findByCreator(eventCreator);
        List<Event> nonConcludedEvents = allEvents.stream()
                                                .filter(event -> !event.getStatus().getStatus().equals(eventStatus.getStatus()))
                                                .collect(Collectors.toList());
        model.addAttribute("eventsList", nonConcludedEvents);
        modelAndView.addObject("currentUser", (Object)eventCreator);
        modelAndView.addObject("fullName", (Object)(eventCreator.getFirstName() + " " + eventCreator.getLastName()));
        return modelAndView;
    }
    
    @RequestMapping(value = { "/concludedeventslist" }, method = { RequestMethod.GET })
    public ModelAndView concludedEventsList(final Model model) {
        ModelAndView modelAndView = new ModelAndView("concludedeventslist");
        User eventCreator = loggedInUser();
        
        EventStatus eventStatus = eventStatusRepo.findByEventStatus(EventStatus.EventStatusEnum.CONCLUDED.toString());
        List<Event> concludedEvents = this.eventRepo.findByCreatorAndStatus(eventCreator, eventStatus);
        
        model.addAttribute("eventsList", concludedEvents);
        modelAndView.addObject("currentUser", eventCreator);
        modelAndView.addObject("fullName", eventCreator.getFirstName() + " " + eventCreator.getLastName());

        return modelAndView;
    }
    
    @RequestMapping(value = { "/tables" }, method = { RequestMethod.GET })
    public ModelAndView tables() {
        ModelAndView modelAndView = new ModelAndView("tables");
        final User user = loggedInUser();
        modelAndView.addObject("currentUser", user);
        modelAndView.addObject("fullName", user.getFirstName() + " " + user.getLastName());
        return modelAndView;
    }
    
    @RequestMapping(value = { "/charts" }, method = { RequestMethod.GET })
    public ModelAndView charts() {
        ModelAndView modelAndView = new ModelAndView("charts");
        User user = loggedInUser();
        modelAndView.addObject("currentUser", user);
        modelAndView.addObject("fullName", user.getFirstName() + " " + user.getLastName());
        return modelAndView;
    }
    
    public User loggedInUser() {
        final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }
        User user = null;
        try {
            user = this.userService.findByUsername(username);
        } catch ( Exception exp ) {
            logger.error(exp.getLocalizedMessage());
            user = null;
        }
        return user;
    }
    
    public JSONObject createUserJson(final User user) throws JSONException {
        final JSONObject personJsonObject = new JSONObject();
        personJsonObject.put("username", user.getUsername());
        personJsonObject.put("email", user.getEmail());
        personJsonObject.put("password", user.getPassword());
        personJsonObject.put("firstName", user.getFirstName());
        personJsonObject.put("lastName", user.getLastName());
        personJsonObject.put("enabled", true);
        personJsonObject.put("roles", user.getRoles());
        return personJsonObject;
    }
}