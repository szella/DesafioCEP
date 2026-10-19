package br.com.szella.desafiocep.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RequestFilter extends OncePerRequestFilter {

    private static final String RESPONSE_TIME_HEADER = "x-response-time-ms";
    private final CorrelationFilter correlationFilter;

    public RequestFilter(CorrelationFilter correlationFilter) {
        this.correlationFilter = correlationFilter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.nanoTime();

        correlationFilter.adicionar(request.getHeader(CorrelationFilter.HTTP_HEADER));

        try {

            response.addHeader(CorrelationFilter.HTTP_HEADER, correlationFilter.getCorrelationId());

            filterChain.doFilter(request, response);
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            response.addHeader(RESPONSE_TIME_HEADER, String.valueOf(durationMs));

            log.info("Request {} finalizada em {} ms com status {}",
                    request.getMethod(), durationMs, response.getStatus());

            correlationFilter.remover();
        }
    }
}
