package org.zanata.client;

import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

/**
 * This rule is a workaround to use PowerMockRunner or any other JUnit runner but wanted JUnit parameterized runner feature.
 * PowerMockRule can not be used due to lack of dependency (powermock-classloading-xstream) in Fedora.
 * The alternative dependency (powermock-classloading-objenesis) doesn't work in java 7.
 *
* @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
public class ParameterRule<T> implements MethodRule
{
   private static final Logger log = LoggerFactory.getLogger(ParameterRule.class);

   private int parameterIndex = 0;
   private List<T> parameters;

   public ParameterRule(T... parameters)
   {
      this.parameters = ImmutableList.copyOf(parameters);
   }

   public T getParameter()
   {
      return parameters.get(parameterIndex);
   }

   @Override
   public Statement apply(final Statement base, FrameworkMethod method, Object target)
   {
      return new Statement()
      {
         public void evaluate()
         {
            for (int i = 0; i < parameters.size(); i++)
            {
               parameterIndex = i;
               try
               {
                  log.debug("running with parameter: {}", parameters.get(parameterIndex));
                  base.evaluate();
               }
               catch (Throwable throwable)
               {
                  throw Throwables.propagate(throwable);
               }
            }
         }
      };
   }
}
