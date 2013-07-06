package edu.stanford.nlp.mt.tools.service.handlers;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;

import com.google.gson.reflect.TypeToken;

import edu.stanford.nlp.mt.tools.service.Messages.BaseReply;
import edu.stanford.nlp.mt.tools.service.Messages.Request;
import edu.stanford.nlp.mt.tools.service.Messages.TranslationRequest;
import edu.stanford.nlp.mt.tools.service.PhrasalServlet;
import edu.stanford.nlp.util.Generics;

public class TranslationRequestHandlerMock implements RequestHandler {

  @Override
  public ServiceResponse handle(Request request) {
    throw new UnsupportedOperationException("This is an asynchronous handler");
  }

  @Override
  public void handleAsynchronous(Request baseRequest,
      HttpServletRequest request, HttpServletResponse response) {
    // Jetty continuations
    // TODO(spenceg) Try Servlet 3.0 if this doesn't work
    Continuation continuation = ContinuationSupport.getContinuation(request);
    continuation.suspend(response); //Start Async Processing

    TranslationRequest translationRequest = (TranslationRequest) baseRequest;
    
    // Translate to uppercase!
    String translation = String.format("(%s-%s) : %s -> %s",
        translationRequest.src.toString(),
        translationRequest.tgt.toString(),
        translationRequest.text,
        translationRequest.text.toUpperCase());
    List<String> translations = Generics.newLinkedList();
    translations.add(translation);
    List<String> alignments = Generics.newLinkedList();
    alignments.add("1-1 2-2 3-3 4-4");
    Type t = new TypeToken<BaseReply>() {}.getType();
    BaseReply baseResponse = new BaseReply(translations, alignments);

    // Simulate a long call to the MT system
    Random random = new Random();
    try {
      Thread.sleep(500 + random.nextInt(1000));
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    ServiceResponse serviceResponse = new ServiceResponse(baseResponse, t);
    request.setAttribute(PhrasalServlet.ASYNC_KEY, serviceResponse);   
    continuation.resume(); // Re-dispatch/ resume to generate response
  }
}
