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
package org.jboss.arquillian.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.jboss.arquillian.maven.delegate.Downloader;

/**
 * Download a Container
 *
 * @goal download
 *
 * @author Davide D'Alto
 * @version $Revision: $
 *
 */
public final class Download extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     */
    MavenProject project;

    /**
     * @component
     */
    RepositorySystem repositorySystem;

    /**
     * @component
     */
    ArchiverManager archiveManager;

    /**
     *
     * @parameter expression="${localRepository}"
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter
     */
    private List<Container> containers;

    /**
     * @parameter expression="${echo.target}" default-value="${project.build.directory}"
     */
    private File unpackDir;

    public String goal() {
        return "download";
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validate();
        initializeContainers(containers);

        Downloader delegate = new Downloader(repositorySystem, localRepository, archiveManager, project);
        delegate.download(containers);
    }

    private void validate() throws AssertionError {
        if (containers == null)
            throw new IllegalArgumentException("At least one container should be selected!");

        if (unpackDir == null)
            throw new AssertionError("unpackDir cannot be null");
    }

    private void initializeContainers(List<Container> containers) {
        for (Container container : containers) {
            if (container.getUnpackDir() == null)
                container.setUnpackDir(unpackDir);
        }
    }

}
