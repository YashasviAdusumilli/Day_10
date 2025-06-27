package org.example.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

import java.util.Random;

public class MongoService {

    private final MongoClient mongoClient;
    private final EmailService emailService;

    public MongoService(Vertx vertx) {
        JsonObject config = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "elective_registration");
        mongoClient = MongoClient.createShared(vertx, config);
        emailService = new EmailService(vertx);
    }

    // 🔹 Generate Random Password
    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    // 🔹 Register Student
    public void registerStudent(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");
        String name = body.getString("name");

        String password = generatePassword();

        JsonObject student = new JsonObject()
                .put("email", email)
                .put("name", name)
                .put("password", password);

        mongoClient.save("students", student, res -> {
            if (res.succeeded()) {
                emailService.sendPasswordEmail(email, password);
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("message", "Registered. Password sent to email.").encode());
            } else {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", "Registration failed").encode());
            }
        });
    }

    // 🔹 Login Student
    public void loginStudent(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");
        String password = body.getString("password");

        JsonObject query = new JsonObject()
                .put("email", email)
                .put("password", password);

        mongoClient.findOne("students", query, null, res -> {
            if (res.succeeded() && res.result() != null) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("message", "Login successful").encode());
            } else {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", "Invalid credentials").encode());
            }
        });
    }
    // 🔹 Get all courses
    public void getCourses(RoutingContext ctx) {
        mongoClient.find("courses", new JsonObject(), res -> {
            if (res.succeeded()) {
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(res.result().toString());
            } else {
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", "Failed to load courses").encode());
            }
        });
    }
    // 🔹 Register for a course and decrease seat count
    public void registerForCourse(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");
        String courseName = body.getString("course");

        JsonObject courseQuery = new JsonObject().put("name", courseName);

        // Step 1: Check if course exists and has seats
        mongoClient.findOne("courses", courseQuery, null, courseResult -> {
            if (courseResult.succeeded() && courseResult.result() != null) {
                JsonObject course = courseResult.result();
                int seats = course.getInteger("seatsAvailable", 0);

                if (seats > 0) {
                    // Step 2: Check if student already registered
                    JsonObject regQuery = new JsonObject()
                            .put("studentEmail", email)
                            .put("courseName", courseName);

                    mongoClient.findOne("registrations", regQuery, null, regCheck -> {
                        if (regCheck.succeeded() && regCheck.result() == null) {
                            // Step 3: Register student
                            JsonObject registration = new JsonObject()
                                    .put("studentEmail", email)
                                    .put("courseName", courseName);

                            mongoClient.save("registrations", registration, regSave -> {
                                if (regSave.succeeded()) {
                                    // Step 4: Decrease seat count
                                    JsonObject update = new JsonObject()
                                            .put("$set", new JsonObject().put("seatsAvailable", seats - 1));

                                    mongoClient.updateCollection("courses", courseQuery, update, updateRes -> {
                                        if (updateRes.succeeded()) {
                                            ctx.response().putHeader("Content-Type", "application/json")
                                                    .end(new JsonObject().put("message", "Course registered").encode());
                                        } else {
                                            ctx.response().end(new JsonObject().put("error", "Failed to update seats").encode());
                                        }
                                    });
                                } else {
                                    ctx.response().end(new JsonObject().put("error", "Failed to save registration").encode());
                                }
                            });
                        } else {
                            ctx.response().end(new JsonObject().put("error", "Already registered for this course").encode());
                        }
                    });

                } else {
                    ctx.response().end(new JsonObject().put("error", "No seats available").encode());
                }
            } else {
                ctx.response().end(new JsonObject().put("error", "Course not found").encode());
            }
        });
    }



    // 🔹 (Next Steps: Get Courses, Register Course - coming soon)
}
