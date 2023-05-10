package br.edu.unifip.ecommerceapi.controllers;

import br.edu.unifip.ecommerceapi.dtos.UserDto;
import br.edu.unifip.ecommerceapi.models.User;
import br.edu.unifip.ecommerceapi.services.UserService;
import br.edu.unifip.ecommerceapi.utils.FileDownloadUtil;
import br.edu.unifip.ecommerceapi.utils.FileUploadUtil;
import br.edu.unifip.ecommerceapi.utils.FileUploadUtilUser;
import br.edu.unifip.ecommerceapi.utils.FlieDownloadUtilUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    final UserService userService;

    public UserController(UserService userService){this.userService = userService;}

    @GetMapping
    public ResponseEntity<List<User>> getAllUser() {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable(value = "id") UUID id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(userOptional.get());
    }

    @PostMapping
    public ResponseEntity<Object> saveUser(@Valid UserDto userDto, HttpServletRequest request) {

        var user = new User();

        BeanUtils.copyProperties(userDto, user);

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("image");

        if (multipartFile != null){
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String uploadDir = "user-image/";

            try {
                String filecode = FileUploadUtilUser.saveFile(fileName, uploadDir, multipartFile);
                user.setImage("/api/user/user-image/" + filecode);
            } catch (IOException e){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image not accepted");
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "id") UUID id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        userService.delete(userOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully.");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable(value = "id") UUID id, HttpServletRequest request) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        Map<Object, Object> objectMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            objectMap.put(entry.getKey(), entry.getValue()[0]);
        }

        String imageUrl = null;
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("image");
        if (multipartFile != null) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String uploadDir = "user-image/";

            try {
                String filecode = FileUploadUtilUser.saveFile(fileName, uploadDir, multipartFile);
                imageUrl = "/api/user/user-image/" + filecode;
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image not accepted");
            }
        }

        if (imageUrl != null) {
            objectMap.put("image", imageUrl);
        }

        userService.partialUpdate(userOptional.get(), objectMap);
        return ResponseEntity.status(HttpStatus.OK).body(userOptional.get());
    }

    @GetMapping("/user-image/{fileCode}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileCode") String fileCode){
        FlieDownloadUtilUser downloadUtilUser = new FlieDownloadUtilUser();

        Resource resource = null;
        try {
            resource = downloadUtilUser.getFileAsResourceUser(fileCode);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        if (resource == null){
            return new ResponseEntity<>("File not found.", HttpStatus.NOT_FOUND);
        }

        MediaType contentType;

        if (Objects.equals(FilenameUtils.getExtension(resource.getFilename()), "jpg")){
            contentType = MediaType.IMAGE_JPEG;
        } else {
            contentType = MediaType.IMAGE_PNG;
        }

        String headerValue = "attchment: filename=\"" + resource.getFilename() + "\"";
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);

    }

}
