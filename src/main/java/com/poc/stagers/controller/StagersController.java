package com.poc.stagers.controller;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.poc.stagers.jwt.JwtTokenProvider;
import com.poc.stagers.models.Event;
import com.poc.stagers.models.EventStatus;
import com.poc.stagers.models.User;
import com.poc.stagers.repositories.EventRepository;
import com.poc.stagers.repositories.EventStatusRepository;
import com.poc.stagers.service.SequenceGeneratorService;
import com.poc.stagers.service.UserServiceImpl;
import com.poc.test.TestContext;
import com.poc.test.annotation.TestPageLink;
import com.poc.test.annotation.TestStager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class StagersController extends StagerCentral
{
    @Autowired
    private UserServiceImpl userService;
    
    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;
    
    @Autowired
    private EventRepository eventRepo;
    
    @Autowired
    private EventStatusRepository eventStatusRepo;

    private static final Logger logger = LogManager.getLogger(StagersController.class);
    
    @RequestMapping(value = { "/stagers" }, method = { RequestMethod.GET })
    public ModelAndView stagers(HttpServletRequest request) {
        super.setRequestContext(request);
        super.setSession(request.getSession());
        super.init(true);
        ModelAndView modelAndView = new ModelAndView();
        User user = loggedInUser();
        if (user != null) {
            StagerCentral.setAttributeInSession(request.getSession(), 
                                                "currentUser", (Object)user);
            StagerCentral.setAttributeInSession(request.getSession(), 
                                                "fullName", user.getFirstName() + " " + user.getLastName());
            // Add user to Test Context
            TestContext.getTestContext(request).put(User.class, user);
        }
        StagerCentral.setAttributeInSession(request.getSession(), "categoryList", (Object)testCategoryList());
        modelAndView.setViewName("stagers");
        return modelAndView;
    }
    

    @RequestMapping(value = { "/stagers/click" }, method = { RequestMethod.POST })
    public ModelAndView click(  HttpServletRequest request, 
                                @RequestParam(value = "categoryName", required = true) String categoryName, 
                                @RequestParam(value = "linkName", required = true) String linkName) {
        return (ModelAndView)super.testUnitLink(categoryName, linkName)
                                        .click(request, new ModelAndView("stagers"));
    }

    
    @RequestMapping(value = { "/stagers" }, method = { RequestMethod.POST })
    public ModelAndView setUser(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(value = "username", required = true) String username) {
        ModelAndView modelAndView = new ModelAndView();
        User user = null;
        try {
            user = userService.findByUsername(username);
        } catch (Exception exp) {
            logger.error(exp.getLocalizedMessage());
            user = null;
        }

        if (null == user) {
            modelAndView.addObject("errorMsg", "No such User exists!!!");
            modelAndView.setViewName("stagers");
        } else {
            // set user in session
            setUserInSession(username);
            // create jwt token and set in cookie
            String jwtToken = userService.refresh(username);
            Cookie cookie = new Cookie(JwtTokenProvider.COOKIE_NAME, jwtToken);
            cookie.setPath("/");
            cookie.setMaxAge(Integer.MAX_VALUE);
            response.addCookie(cookie);

            // Add user to Test Context
            TestContext.getTestContext(request).put(User.class, user);
            StagerCentral.setAttributeInSession(request.getSession(), 
                                                "currentUser", (Object)user);
            StagerCentral.setAttributeInSession(request.getSession(), 
                                                "fullName", user.getFirstName() + " " + user.getLastName());
            StagerCentral.setAttributeInSession(request.getSession(),
                                                "name", user.getUsername());
            modelAndView.setViewName("stagers");
            
        }
        return modelAndView;
    }
    

    @TestStager(typeList = "Buyer Actions", description = "Create User Doyen")
    @RequestMapping(value = { "/stagers/createuserstager" }, method = { RequestMethod.GET })
    public ModelAndView createUser() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("createuserstager");
        return modelAndView;
    }
    
    @RequestMapping(value = { "/stagers/createuserstager" }, method = { RequestMethod.POST })
    public ModelAndView createUser( HttpServletRequest request, 
                                    @RequestParam(value = "username", required = true) String username) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = null;

        try {
            userExists = userService.findByUsername(username);
        } catch (Exception exp) {
            logger.error(exp.getLocalizedMessage());
            userExists = null;
        }

        if (userExists != null) {
            modelAndView.addObject("errorMsg", "There is already a user registered with the username provided!");
            modelAndView.setViewName("createuserstager");
        } else {
            logger.info("Creating User from stager: " + username);
            User user = new User(username, username, "Test", "User", "Welcome1a");
            user.setId(sequenceGeneratorService.generateSequence("users_sequence"));
            userService.createUser(user);
            modelAndView.addObject("successMsg", "User " + username + " created successfully");
            modelAndView.setViewName("stagers");
        }
        return modelAndView;
    }

    
    @TestStager(typeList = "Buyer Actions", description = "Create Event Doyen")
    @RequestMapping(value = { "/stagers/createeventstager" }, method = { RequestMethod.GET })
    public ModelAndView createEvent(HttpServletRequest request, User user) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("createeventstager");
        return modelAndView;
    }
    
    @RequestMapping(value = { "/stagers/createeventstager" }, method = { RequestMethod.POST })
    public ModelAndView createEvent(HttpServletRequest request, 
                                    @RequestParam(value = "eventId", required = true) String eventId, 
                                    @RequestParam(value = "status", required = true) String status, 
                                    @RequestParam(value = "title", required = true) String title, 
                                    @RequestParam(value = "amount", required = true) String amount, 
                                    @RequestParam(value = "description", required = true) String description) {
        
        ModelAndView modelAndView = new ModelAndView();
        logger.info("Creating Event from stager: " + eventId);
        EventStatus eventStatus = eventStatusRepo.findByEventStatus(status);
        User user = loggedInUser();
        if (user == null) {
            modelAndView.addObject("errorMsg", (Object)"No Logged in User, cannot create event");
            modelAndView.setViewName("stagers");
        } else {
            Event event = new Event(eventId, title, description, new BigDecimal(amount), eventStatus, user);
            event.setId(sequenceGeneratorService.generateSequence("event_sequence"));
            eventRepo.save(event);
            modelAndView.addObject("successMsg", "Event " + eventId + " created successfully");
            modelAndView.setViewName("stagers");
            TestContext.getTestContext(request).put(Event.class, event);
        }
        return modelAndView;
    }

    @TestStager(typeList = "Buyer Actions", description = "Load Event Doyen")
    @RequestMapping(value = { "/stagers/loadeventstager" }, method = { RequestMethod.GET })
    public ModelAndView loadEvent(User user) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("loadevent");
        return modelAndView;
    }

    @RequestMapping(value = { "/stagers/loadeventstager" }, method = { RequestMethod.POST })
    public ModelAndView loadEvent(HttpServletRequest request, 
                                    @RequestParam(value = "eventid", required = true) String eventId) {        
        ModelAndView modelAndView = new ModelAndView();
        logger.info("Loading Event into Test Context: " + eventId);
        User user = loggedInUser();
        if (user == null) {
            modelAndView.addObject("errorMsg", (Object)"No Logged in User, cannot load event");
            modelAndView.setViewName("stagers");
        } else {
            List<Event> eventList = eventRepo.findByCreatorAndEventid(user, eventId);
            if (eventList == null || eventList.isEmpty()) {
                modelAndView.addObject("errorMsg", "No such Event exists!!!");
                modelAndView.setViewName("loadevent");
            } else {
                // Always load the first event from the list as event id can be same
                Event event = (Event)eventList.get(0);
                modelAndView.addObject("successMsg", "Event " + eventId + " loaded into context successfully");
                modelAndView.setViewName("stagers");
                TestContext.getTestContext(request).put(Event.class, event);
            }
        }
        return modelAndView;
    }
    

    /**
     * Get Logged In user from Security Context Principal
     * @return
     */
    public User loggedInUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
    
    /**
     * Helper method sets the given user in session
     * @param username
     */
    public static void setUserInSession(String username) {
        List<GrantedAuthority> authorities = (List<GrantedAuthority>)AuthorityUtils
                                                        .createAuthorityList(new String[] { "ADMIN" });
        Authentication authentication = (Authentication)
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @TestPageLink(typeList = "Buyer Actions", description = "Go To Dashboard")
    @RequestMapping(value = { "/stagers/dashboardpage" }, method = { RequestMethod.GET })
    public ModelAndView dashboardPage(HttpServletRequest request, User user) {
        ModelAndView modelAndView = new ModelAndView("dashboard");
        return modelAndView;
    }
    
    @TestPageLink(typeList = "Buyer Actions", description = "Go To Open Events List")
    @RequestMapping(value = { "/stagers/openeventslistpage" }, method = { RequestMethod.GET })
    public ModelAndView openEventsListPage(HttpServletRequest request, User user) {
        ModelAndView modelAndView = new ModelAndView("openeventslist");
        return modelAndView;
    }
    
    @TestPageLink(typeList = "Buyer Actions", description = "Go To Concluded Events List")
    @RequestMapping(value = { "/stagers/concludedeventslistpage" }, method = { RequestMethod.GET })
    public ModelAndView concludedEventsListPage(HttpServletRequest request, User user) {
        ModelAndView modelAndView = new ModelAndView("concludedeventslist");
        return modelAndView;
    }

    @TestPageLink(typeList = "Buyer Actions", description = "Go To Event Detail Page")
    @RequestMapping(value = { "/stagers/eventdetailspage" }, method = { RequestMethod.GET })
    public ModelAndView eventDetailPage(HttpServletRequest request, User user, Event event) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/eventdetails?eventid=" + event.getId());
        return modelAndView;
    }
    
}