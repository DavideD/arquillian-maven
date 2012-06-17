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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.jboss.arquillian.maven.Container;
import org.jboss.arquillian.maven.delegate.exception.FileNotDownloadedException;
import org.jboss.arquillian.maven.delegate.json.DetailsContainer;

public class Downloader {

    private final ArtifactResolver resolver;

    private final ContainersConfiguration containersConfiguration;

    private final ArchiveExporter exporter;

    private final UrlGetter urlGetter;

    public Downloader(RepositorySystem repositorySystem, ArtifactRepository localRepository, ArchiverManager archiveManager,
            MavenProject project) {
        this.resolver = new ArtifactResolver(repositorySystem, localRepository, archiveManager, project);
        this.containersConfiguration = new ContainersConfiguration();
        this.urlGetter = new UrlGetter();
        this.exporter = new ArchiveExporter(archiveManager);
    }

    public void download(List<Container> arquillianContainers) throws MojoExecutionException {
        for (Container selectedContainer : arquillianContainers) {
            unpack(selectedContainer);
        }
    }

    public void unpack(Container selectedContainer) throws MojoExecutionException {
        if (selectedContainer.getUrl() == null) {
            DetailsContainer details = containersConfiguration.loadDetails(selectedContainer);
            if (isDownloadableFromRepository(details)) {
                resolver.unpackFromRepository(details, selectedContainer);
            } else {
                String url = url(details, selectedContainer.getVersion());
                File downloaded = downloadFromUrl(url, selectedContainer.getUnpackDir(), fileName(url));
                exporter.unpack(downloaded, selectedContainer.getUnpackDir());
            }
        } else {
            File downloaded = downloadFromUrl(selectedContainer.getUrl(), selectedContainer.getUnpackDir(), fileName(selectedContainer.getUrl()));
            exporter.unpack(downloaded, selectedContainer.getUnpackDir());
        }
    }

    private String fileName(String url) throws MojoExecutionException {
        try {
            return new File(new URL(url).getFile()).getName();
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Malformed URL: " + url, e);
        }
    }

    private File downloadFromUrl(String url, File outputDir, String outputFileName) throws MojoExecutionException {
        try {
            return urlGetter.getFile(url, outputDir, outputFileName);
        } catch (FileNotDownloadedException e) {
            throw new MojoExecutionException("Download failed: " + e.getMessage(), e);
        }
    }

    private String url(DetailsContainer selectedContainer, String containerVersion) {
        return selectedContainer.getDownload().getUrl().replaceAll("(\\$\\{.*?\\})", containerVersion);
    }

    private boolean isDownloadableFromRepository(DetailsContainer selectedContainer) {
        return selectedContainer.getDownload().getUrl() == null;
    }

}
