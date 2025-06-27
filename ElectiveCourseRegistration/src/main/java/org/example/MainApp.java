package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.example.service.MongoService;
import org.example.service.EmailService;

public class MainApp extends AbstractVerticle {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainApp());
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);
        MongoService mongoService = new MongoService(vertx);
        EmailService emailService = new EmailService(vertx);

        router.route().handler(BodyHandler.create());

        // Register student and send email
        router.post("/register").handler(mongoService::registerStudent);

        // Login
        router.post("/login").handler(mongoService::loginStudent);

        // Get courses
        router.get("/courses").handler(mongoService::getCourses);

        // Register for a course
        router.post("/register-course").handler(mongoService::registerForCourse);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888)
                .onSuccess(s -> System.out.println("Server started on port 8888"));
    }
}
