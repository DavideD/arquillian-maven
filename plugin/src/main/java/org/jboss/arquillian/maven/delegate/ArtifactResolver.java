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

import java.io.File;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.jboss.arquillian.maven.Container;
import org.jboss.arquillian.maven.delegate.json.DetailsContainer;

public class ArtifactResolver {

    private final ArtifactRepository localRepository;
    private final RepositorySystem repositorySystem;
    private final MavenProject project;
    private final ArchiverManager archiveManager;

    public ArtifactResolver(RepositorySystem repositorySystem, ArtifactRepository localRepository,
            ArchiverManager archiveManager, MavenProject project) {
        this.localRepository = localRepository;
        this.repositorySystem = repositorySystem;
        this.archiveManager = archiveManager;
        this.project = project;
    }

    public void unpackFromRepository(DetailsContainer selectedContainer, Container container) throws MojoExecutionException {
        ArtifactResolutionRequest request = createArtifactResolutionRequest(selectedContainer, container);
        unpack(request, container.getUnpackDir());
    }

    public ArtifactResolutionRequest createArtifactResolutionRequest(DetailsContainer selectedContainer, Container container) {
        Artifact toDownload = createArtifact(selectedContainer, container);
        ArtifactResolutionRequest request = createRequest(toDownload);
        return request;
    }

    private Artifact createArtifact(DetailsContainer selectedContainer, Container container) {
        Artifact toDownload = repositorySystem.createArtifact(selectedContainer.getDownload().getGroup_id(), selectedContainer
                .getDownload().getArtifact_id(), container.getVersion(), "zip");
        return toDownload;
    }

    private void unpack(ArtifactResolutionRequest request, File containerOutputDir) throws MojoExecutionException {
        ArtifactResolutionResult resolve = repositorySystem.resolve(request);
        Set<Artifact> artifacts = resolve.getArtifacts();
        Artifact containerArtifact = artifacts.iterator().next();
        unpack(containerArtifact.getFile(), containerOutputDir);
    }

    private ArtifactResolutionRequest createRequest(Artifact toDownload) {
        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setArtifact(toDownload);
        request.setResolveTransitively(false);
        request.setRemoteRepositories(project.getRemoteArtifactRepositories());
        request.setLocalRepository(localRepository);
        return request;
    }

    private void unpack(File request, File containerOutputDir) throws MojoExecutionException {
        try {
            UnArchiver unArchiver = archiveManager.getUnArchiver(request);
            unArchiver.setSourceFile(request);
            containerOutputDir.mkdirs();
            unArchiver.setDestDirectory(containerOutputDir);
            unArchiver.extract();
        } catch (NoSuchArchiverException e) {
            throw new MojoExecutionException("Error unapcking the archive: " + request, e);
        }
    }

}
