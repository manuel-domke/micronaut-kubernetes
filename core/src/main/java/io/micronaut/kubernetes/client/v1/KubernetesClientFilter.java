/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.kubernetes.client.v1;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * HTTP Client filter which includes an Authorization HTTP Header with value "Bearer XXXX", Being XXXX the token saved in the file {@value #TOKEN_PATH} in every request against the Kubernetes API.
@Filter(value = "/api/v1/**", serviceId = KubernetesClient.SERVICE_ID)
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.0.0
 */
@Requires(env = Environment.KUBERNETES)
@Requires(condition = TokenFileExists.class)
@Requires(property = KubernetesConfiguration.PREFIX + ".client-filter.enabled", notEquals = StringUtils.FALSE)
public class KubernetesClientFilter implements HttpClientFilter {

    public static final String TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    private final String token;

    /**
     *
     * @throws IOException if an exception occurs while reading the content of file (@value #TOKEN_PATH).
     */
    public KubernetesClientFilter() throws IOException {
        this.token = new String(Files.readAllBytes(Paths.get(TOKEN_PATH)));
    }

    /**
     * A variation of {@link io.micronaut.http.filter.HttpFilter#doFilter(io.micronaut.http.HttpRequest, io.micronaut.http.filter.FilterChain)} that receives a {@link MutableHttpRequest}
     * allowing the request to be modified.
     *
     * @param request The request
     * @param chain   The filter chain
     * @return The publisher of the response
     * @see io.micronaut.http.filter.HttpFilter
     */
    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        return chain.proceed(request.bearerAuth(token));
    }
}
