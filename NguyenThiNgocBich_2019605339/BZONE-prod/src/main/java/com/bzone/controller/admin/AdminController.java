package com.bzone.controller.admin;

import com.bzone.model.Role;
import com.bzone.model.User;
import com.bzone.repository.RoleRepository;
import com.bzone.repository.UserRepository;
import com.bzone.service.DefaultEmailService;
import com.bzone.service.DefaultUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@Transactional
public class AdminController {

    @Autowired
    private DefaultEmailService emailService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DefaultUserService userService;

    @GetMapping("/admin")
    public String index(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        List<User> admins = userRepository.findAllByRole(roleRepository.findByRoleName("Admin"));
        List<User> users = userRepository.findAll();
        users.removeAll(admins);
        model.addAttribute("user", user);
        model.addAttribute("users", users);
        return "pages/admin/dashboard";
    }

    @GetMapping("/admin/create-account")
    public String show(Model model, @RequestParam(name = "success", defaultValue = "false") boolean success) {
        User user = new User();
        model.addAttribute("user", user);
        List<Role> roles = roleRepository.findAll();
        roles.remove(roleRepository.findByRoleName("Admin"));
        roles.remove(roleRepository.findByRoleName("Customer"));
        model.addAttribute("roles", roles);
        model.addAttribute("success", success);
        return "pages/admin/create_account";
    }

    @PostMapping("/admin/create-account")
    public String create(Model model, @Valid User user, BindingResult result, @RequestParam String role_id, @RequestParam(name = "date") String dob) throws ParseException {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String email = user.getEmail();
        String phone = user.getPhoneNumber();
        String address = user.getAddress();
        Date dateOfBirth = null;
        if (dob != null && !dob.isEmpty()) {
            dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(dob);
        }
        boolean active = true;
        boolean gender = user.isGender();
        Role role = roleRepository.findById(Long.parseLong(role_id));
        if(firstName==null || firstName.isEmpty()) {
            result.rejectValue("firstName", "firstName", "First Name is required.");
        }
        if(lastName==null || lastName.isEmpty()) {
            result.rejectValue("firstName", "firstName", "Last Name is required.");
        }
        if(email != null && !email.isEmpty() && userRepository.findByEmail(email)!=null) {
            result.rejectValue("email", "email", "Email already exists.");
        }
        if(phone == null || phone.isEmpty()) {
            result.rejectValue("phoneNumber", "error.user", "Phone Number is required.");
        }
        if(address == null || address.isEmpty()) {
            result.rejectValue("address", "error.user", "Address is required.");
        }
        if(result.hasFieldErrors("email") && result.hasFieldErrors("phoneNumber") && result.hasFieldErrors("address") && result.hasFieldErrors("firstName") && result.hasFieldErrors("lastName")) {
            List<Role> roles = roleRepository.findAll();
            roles.remove(roleRepository.findByRoleName("Admin"));
            roles.remove(roleRepository.findByRoleName("Customer"));
            model.addAttribute("roles", roles);
            model.addAttribute("success", false);
            return "pages/admin/create_account";
        }
        user.setDob(dateOfBirth);
        user.setActive(active);
        user.setRole(role);
        String password = "123456@Aa";
        user.setPassword(password);
        System.out.println(user.getPassword());
        try{
            userService.addUser(user);
        } catch (Exception e) {
            List<Role> roles = roleRepository.findAll();
            roles.remove(roleRepository.findByRoleName("Admin"));
            roles.remove(roleRepository.findByRoleName("Customer"));
//            result.rejectValue("email", "email", "Email đã tồn tại");
            model.addAttribute("roles", roles);
            return "pages/admin/create_account";
        }
        //send password to mail
        try {
            String message = "<div style=\"font-family: Helvetica,Arial,sans-serif;min-width:1000px;overflow:auto;line-height:2\">\n" +
                    "  <div style=\"margin:50px auto;width:70%;padding:20px 0\">\n" +
                    "    <div style=\"border-bottom:1px solid #eee\">\n" +
                    "      <a href=\"\" style=\"font-size:1.4em;color: #C92127;text-decoration:none;font-weight:600\">BZone</a>\n" +
                    "    </div>\n" +
                    "    <p style=\"font-size:1.1em\">Hi,</p>\n" +
                    "    <p>Hi s. Use the following password to login into the system.</p>\n" +
                    "    <h2 style=\"background: #C92127;margin: 0 auto;width: max-content;padding: 0 10px;color: #fff;border-radius: 4px;\">" +
                    password +
                    "</h2>\n" +
                    "    <p style=\"font-size:0.9em;\">Regards,<br />BZone</p>\n" +
                    "    <hr style=\"border:none;border-top:1px solid #eee\" />\n" +
                    "    <div style=\"float:right;padding:8px 0;color:#aaa;font-size:0.8em;line-height:1;font-weight:300\">\n" +
                    "      <p>BZone Inc</p>\n" +
                    "      <p>1600 Amphitheatre Parkway</p>\n" +
                    "      <p>California</p>\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</div>";

            emailService.sendEmail(user.getEmail(), "BZone - Password", message);
        } catch (Exception e) {

        }

        model.addAttribute("success", "Account created successfully.");
        return "redirect:/admin/create-account?success=true";
    }

    @GetMapping ("/admin/view-account")
    public String view(Model model, @RequestParam(name="email", defaultValue = "") String email, @RequestParam(name="role-id", defaultValue = "0") String id) {
        int roleId = Integer.parseInt(id);
        List<User> users;
        if(roleId==0) {
            users = userRepository.findAllByEmail(email);
            users.removeAll(userRepository.findAllByRole(roleRepository.findByRoleName("Admin")));
        } else {
            users = userRepository.findAllByEmailAndRoleId(email, roleId);
        }
        List<Role> roles = roleRepository.findAll();
        roles.remove(roleRepository.findByRoleName("Admin"));
        model.addAttribute("roles", roles);
        model.addAttribute("users", users);
        model.addAttribute("email", email);
        model.addAttribute("id", roleId);
        return "pages/admin/view_account";
    }
    @GetMapping("/admin/view-detail")
    public String viewDetail(Model model, @RequestParam(name="id") long id ) {
        User user = userRepository.findById(id);
        model.addAttribute("user", user);
        return "pages/admin/view_detail";
    }

    @GetMapping("/admin/activate")
    public String activate(Model model, @RequestParam(name="id") long id ) {
        User user = userRepository.findById(id);
        user.setActive(!user.isActive());
        userRepository.save(user);
        return "redirect:/admin/view-account";
    }
}
