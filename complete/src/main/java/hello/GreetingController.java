package hello;

import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.HtmlUtils;

import java.util.concurrent.Callable;

@Controller
public class GreetingController {

    private DeferredResult<String> deferredResult;
    private SseEmitter sseEmitter;
    private ResponseBodyEmitter emitter;

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }

    @GetMapping("/quotes")
    @ResponseBody
    public DeferredResult<String> quotes() {
        DeferredResult<String> deferredResult = new DeferredResult<>();
        this.deferredResult = deferredResult;
        return deferredResult;
    }

    @GetMapping("/release")
    @ResponseBody
    public String release() {
        this.deferredResult.setResult("test");
        return "OK";
    }

    @PostMapping("/upload")
    @ResponseBody
    public Callable<String> processUpload() {
        System.out.println("controller: " + Thread.currentThread().getName());
        return () -> {
            System.out.println("callable: " + Thread.currentThread().getName());
            return "something";
        };
    }

    @GetMapping(path="/events/sse", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter();
        this.sseEmitter = emitter;
        return emitter;
    }

    @GetMapping("/events")
    public ResponseBodyEmitter handle() {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        this.emitter = emitter;
        return emitter;
    }

    @ResponseBody
    @GetMapping("/events/send")
    public String sendEvent() {
        try {
            emitter.send("Hello " + Thread.currentThread().getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            sseEmitter.send("Hello " + Thread.currentThread().getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "OK";
    }

    @ResponseBody
    @GetMapping("/events/complete")
    public String completeEvents() {
        try {
            emitter.complete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            sseEmitter.complete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "OK";
    }



}
