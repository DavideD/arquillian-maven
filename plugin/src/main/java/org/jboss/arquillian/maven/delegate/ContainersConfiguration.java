/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.maven.delegate;

import java.io.InputStream;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.arquillian.maven.Container;
import org.jboss.arquillian.maven.delegate.json.DetailsContainer;

public class ContainersConfiguration {

    public DetailsContainer loadDetails(Container arquillianContainer) throws MojoExecutionException {
        List<DetailsContainer> containers = loadAvailableContainers();
        String regex = regEx(arquillianContainer);
        for (DetailsContainer selected : containers) {
            String artifact_id = selected.getArtifact_id();
            if (artifact_id.matches(regex)) {
                return selected;
            }
        }

        throw new MojoExecutionException("Container profile " + arquillianContainer + " not recognized. Available containers: "
                + containers);
    }

    private List<DetailsContainer> loadAvailableContainers() throws MojoExecutionException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<List<DetailsContainer>> valueTypeRef = new TypeReference<List<DetailsContainer>>() {
            };
            List<DetailsContainer> containers = objectMapper.readValue(containersStream(), valueTypeRef);
            return containers;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private InputStream containersStream() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("containers.json");
    }

    private String regEx(Container arquillianContainer) {
        String id = arquillianContainer.getType().toLowerCase();
        if (id.replaceAll("_", "").equals("jbossas") && arquillianContainer.getVersion().startsWith("7")) {
            return "jboss-as-arquillian-container-managed";
        }
        return "arquillian-" + id + "-managed-" + arquillianContainer.getVersion().charAt(0) + ".*";
    }

}
