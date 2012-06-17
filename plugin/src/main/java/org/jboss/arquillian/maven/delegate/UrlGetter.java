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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.maven.delegate.exception.FileNotDownloadedException;

public class UrlGetter {

    public File getFile(String url, File outputDir, String outputFileName) throws FileNotDownloadedException {
        FileOutputStream stream = null;
        try {
            outputDir.mkdirs();
            File downloadedFile = new File(outputDir, outputFileName);
            stream = new FileOutputStream(downloadedFile);
            IOUtils.copy(new URL(url).openStream(), stream);
            return downloadedFile;
        } catch (MalformedURLException e) {
            throw new FileNotDownloadedException(e);
        } catch (IOException e) {
            throw new FileNotDownloadedException(e);
        } finally {
            close(stream);
        }
    }

    private void close(FileOutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
