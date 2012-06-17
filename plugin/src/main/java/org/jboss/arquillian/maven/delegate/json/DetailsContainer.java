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
package org.jboss.arquillian.maven.delegate.json;

import java.util.List;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class DetailsContainer {
    private String group_id;
    private String artifact_id;

    private String name;
    private List<DependencyDescription> dependencies;
    private DependencyDescription download;
    private List<Configuration> configurations;

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getArtifact_id() {
        return artifact_id;
    }

    public void setArtifact_id(String artifact_id) {
        this.artifact_id = artifact_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DependencyDescription> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<DependencyDescription> dependencies) {
        this.dependencies = dependencies;
    }

    public DependencyDescription getDownload() {
        return download;
    }

    public void setDownload(DependencyDescription download) {
        this.download = download;
    }

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Configuration> configurations) {
        this.configurations = configurations;
    }

    public String getId() {
        return name.replace("Arquillian Container ", "").replaceAll(" ", "_").toUpperCase();
    }

    @Override
    public String toString() {
        return getId();
    }

}
