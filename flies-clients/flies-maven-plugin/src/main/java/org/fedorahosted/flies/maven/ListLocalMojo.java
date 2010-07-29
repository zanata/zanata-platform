package org.fedorahosted.flies.maven;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Lists all local files in the project which are considered to be Flies
 * documents. These are the files which will be sent to Flies when using the
 * 'publish' goal.
 * 
 * @goal listlocal
 */
public class ListLocalMojo extends AbstractFliesMojo
{

   public ListLocalMojo() throws Exception
   {
      super();
   }

   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {
      super.execute();
      // TODO Auto-generated method stub
   }

}
