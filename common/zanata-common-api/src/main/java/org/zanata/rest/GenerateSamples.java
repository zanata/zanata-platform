package org.zanata.rest;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationException;


import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.zanata.common.Namespaces;
import org.zanata.rest.MediaTypes.Format;
import org.zanata.rest.dto.HasMediaType;
import org.zanata.rest.dto.HasSample;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.dto.ProjectList;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.resource.ResourceMetaList;
import org.zanata.rest.dto.resource.TranslationsResource;

//import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;

public class GenerateSamples
{

   private final ObjectMapper mapper;
   private final PrintStream out;

   private final PrintStream noCloseOut;

   public GenerateSamples(PrintStream out)
   {
      this.out = out;
      mapper = new ObjectMapper();
      mapper.getSerializationConfig().enable(Feature.INDENT_OUTPUT);

      noCloseOut = new PrintStream(out)
      {
         @Override
         public void close()
         {
         }
      };
   }

   public void run() throws ValidationException, JsonGenerationException, JsonMappingException, IOException
   {

      // Projects Resource
      h1("Project Resource");
      write("`GET /projects/`", ProjectList.class);
      write("`PUT /projects/p/{id}`", Project.class);

      h1("Project Iteration Resource");
      // ProjectIteration Resource
      write("`GET /projects/{id}/iterations/{id}`", ProjectIteration.class);

      // Translation Resource
      h1("Translation Resource");
      write("`GET ./`", ResourceMetaList.class);
      write("`GET ./r/{res}`", TranslationsResource.class);

      h1("People Resource");

      // write("Person inline:", PersonInline.class);

   }

   private void h1(String message)
   {
      out.println("= " + message + " =");
   }

   private void h2(String message)
   {
      out.println("== " + message + " ==");
   }

   private void h3(String message)
   {
      out.println("=== " + message + " ===");
   }

   private <T extends HasSample<T>> void write(String heading, Class<T> clazz) throws JsonGenerationException, JsonMappingException, IOException, ValidationException
   {

      h2(heading);
      T obj = create(clazz);
      validateEntity((Serializable) obj);
      out.println("*Specified by class:* `" + obj.getClass().getCanonicalName() + "`");
      out.println();
      h3("Json Example");
      if (obj instanceof HasMediaType)
      {
         out.print("*Media Type:* `");
         out.print(((HasMediaType) obj).getMediaType(Format.JSON));
         out.print("`");
      }
      out.println();
      out.println("{{{");
      mapper.writeValue(noCloseOut, obj);
      out.println();
      out.println("}}}");
      out.println();
      h3("XML example");
      if (obj instanceof HasMediaType)
      {
         out.print("*Media Type:* `");
         out.print(((HasMediaType) obj).getMediaType(Format.XML));
         out.print("`");
      }
      out.println();
      out.println("{{{");
      try
      {
         JAXBContext context = JAXBContext.newInstance(obj.getClass());
         Marshaller m = context.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
         // m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
         // namespacePrefixMapper);
         m.marshal(obj, out);

      }
      catch (JAXBException e)
      {
         throw new RuntimeException(e);
      }
      out.println("}}}");
      out.println();
   }

   // static final NamespacePrefixMapper namespacePrefixMapper = new
   // NamespacePrefixMapper() {
   //
   // @Override
   // public String getPreferredPrefix(String namespaceUri,
   // String suggestion, boolean requirePrefix) {
   // if (namespaceUri == null || namespaceUri.isEmpty() ) {
   // return "";
   // }
   // if (namespaceUri.equalsIgnoreCase(Namespaces.ZANATA_API)) {
   // return "";
   // }
   // if (namespaceUri.equalsIgnoreCase(PoHeader.NAMESPACE)) {
   // return "po";
   // }
   // if (namespaceUri
   // .equalsIgnoreCase("http://www.w3.org/2001/XMLSchema-instance")) {
   // return "xsi";
   // }
   // return suggestion;
   // }
   //
   // @Override
   // public String[] getPreDeclaredNamespaceUris() {
   // return new String[] {};
   // }
   // };

   private <T extends HasSample<T>> T create(Class<T> clazz)
   {
      try
      {
         return clazz.newInstance().createSample();
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }

   @SuppressWarnings("unchecked")
   private static <T extends Serializable> void validateEntity(T entity) throws ValidationException
   {
      @SuppressWarnings("rawtypes")
      ClassValidator<T> validator = new ClassValidator(entity.getClass());
      if (validator.hasValidationRules())
      {
         InvalidValue[] invalidValues = validator.getInvalidValues(entity);
         if (invalidValues.length != 0)
         {
            StringBuilder message = new StringBuilder();
            message.append("Request body contains invalid values:\n");
            for (InvalidValue invalidValue : invalidValues)
            {
               message.append(invalidValue.getPropertyPath());
               message.append(">");
               message.append(invalidValue.getPropertyName());
               message.append(": ");
               message.append(invalidValue.getMessage());
               message.append("\n");
            }
            throw new ValidationException(message.toString());
         }
      }
   }

   public static void main(String[] args) throws ValidationException, JsonGenerationException, JsonMappingException, IOException
   {
      new GenerateSamples(System.out).run();
   }

}
