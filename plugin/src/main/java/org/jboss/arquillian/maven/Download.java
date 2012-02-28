/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.maven.json.Dependency;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;

/**
 * Download and unpack a container
 * 
 * @goal download
 * 
 * @author Davide D'Alto
 * @version $Revision: $
 * 
 */
public final class Download extends BaseCommand
{

   /**
    * The version of the container to download.
    *
    * @parameter expression="${containerVersion}"
    */
   public String containerVersion;

   /**
    * The directory where to download the container.
    *
    * @parameter expression="${containerDestinationFolder}" default-value="${project.build.directory}"
    */
   public File containerDestinationFolder;

   /**
    * @parameter expression="${containerDestinationName}"
    */
   public String containerDestinationName;

   /**
    * @component
    * @readonly
    */
   private ArtifactResolver artifactResolver;

   /**
    * To look up Archiver/UnArchiver implementations
    * 
    * @component
    */
   protected ArchiverManager archiverManager;

   /**
    * List of Remote Repositories used by the resolver
    * 
    * @parameter expression="${project.remoteArtifactRepositories}"
    * @readonly
    * @required
    */
   protected List<ArtifactRepository> remoteRepositories;

   /**
    * Location of the local repository.
    * 
    * @parameter expression="${localRepository}"
    * @readonly
    * @required
    */
   protected ArtifactRepository localRepository;

   /**
    * @component
    */
   private RepositorySystem repositorySystem;
   
   @Override
   void validateInput()
   {
      // No need to validate for download
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.maven.BaseCommand#goal()
    */
   public String goal()
   {
      return "download";
   }
   
   @Override
   void loadContainer(Class<?>... extensions) throws Exception {
      Artifact arquillianArtifact = findContainerArtifact();
      try
      {
         ObjectMapper objectMapper = new ObjectMapper();
         List<org.jboss.arquillian.maven.json.Container> readValue = objectMapper.readValue(getUrl(),
               new TypeReference<List<org.jboss.arquillian.maven.json.Container>>()
               {
               });
         for (org.jboss.arquillian.maven.json.Container con : readValue)
         {
            if (arquillianArtifact.getGroupId().equals(con.getGroup_id())
                  && arquillianArtifact.getArtifactId().equals(containerName(con)))
            {
               download(con, containerVersion);
               return;
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }      
   };
   
   @Override
   public void perform(Manager manager, Container container)
   {
      throw new UnsupportedOperationException();
   }

   private void download(org.jboss.arquillian.maven.json.Container con, String version) throws Exception
   {
      createDestionationFolder();
      if (isArtifact(con))
      {
         unpackArtifact(con, version);
      }
      else
      {
         downloadFromUrl(con);
      }
   }

   private boolean isArtifact(org.jboss.arquillian.maven.json.Container con)
   {
      return con.getDownload().getUrl() == null;
   }

   private void createDestionationFolder()
   {
      containerDestinationFolder.mkdirs();
   }

   private void downloadFromUrl(org.jboss.arquillian.maven.json.Container con) throws MalformedURLException, IOException, FileNotFoundException
   {
      Dependency download = con.getDownload();
      String url = download.getUrl().replaceAll("\\$\\{version\\.org\\.apache\\.tomcat\\}", containerVersion);
      getLog().info("Downloading: " + url);
      URL google = new URL(url);
      ReadableByteChannel rbc = Channels.newChannel(google.openStream());
      File compressedContainer = new File(containerDestinationFolder, containerName(con) + ".zip");
      FileOutputStream fos = new FileOutputStream(compressedContainer);
      fos.getChannel().transferFrom(rbc, 0, 1 << 24);
      ShrinkWrap.create(ZipImporter.class).importFrom(compressedContainer).as(ExplodedExporter.class)
            .exportExploded(containerDestinationFolder, containerName(con));
   }

   private String containerName(org.jboss.arquillian.maven.json.Container con)
   {
      if (containerDestinationName == null)
         return con.getArtifact_id();
      
      return containerDestinationName;
   }

   private void unpackArtifact(org.jboss.arquillian.maven.json.Container con, String version) throws NoSuchArchiverException
   {
      Dependency download = con.getDownload();
      getLog().info("Perform download of " + download.getArtifact_id());
      Artifact artifact = repositorySystem.createArtifact(download.getGroup_id(), download.getArtifact_id(), version, "zip");
      ArtifactResolutionRequest request = new ArtifactResolutionRequest();
      request.setArtifact(artifact);
      request.setLocalRepository(localRepository);
      request.setRemoteRepositories(remoteRepositories);
      artifactResolver.resolve(request);
      getLog().info("Artifact resolved");
      unpack(artifact);
   }

   private void unpack(Artifact artifact) throws NoSuchArchiverException
   {
      File file = artifact.getFile();
      UnArchiver unArchiver = archiverManager.getUnArchiver(file);
      unArchiver.setSourceFile(file);
      unArchiver.setDestDirectory(containerDestinationFolder);
      unArchiver.setOverwrite(false);
      unArchiver.extract();
   }

   private static final List<String> containerGroups = Arrays.asList("org.jboss.as", "org.jboss.arquillian.container");

   private Artifact findContainerArtifact()
   {
      Set<Artifact> dependencies = getDependencies();
      for (Artifact artifact : dependencies)
      {
         if (containerGroups.contains(artifact.getGroupId()))
               return artifact;
      }
      return null;
   }

   private URL getUrl()
   {
      try
      {
         return this.getClass().getClassLoader().getResource("containers.json").toURI().toURL();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }
}
