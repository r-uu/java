package de.ruu.lib.junit;

import de.ruu.lib.util.IO;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Optional;

public class DisabledOnServerNotListeningCondition implements ExecutionCondition
{
	private static final Logger log = LoggerFactory.getLogger(DisabledOnServerNotListeningCondition.class);

	public DisabledOnServerNotListeningCondition() { }

	@Override public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context)
	{
		Optional<AnnotatedElement> optionalAnnotatedElement = context.getElement();

		if (optionalAnnotatedElement.isPresent())
		{
			AnnotatedElement annotatedElement = optionalAnnotatedElement.get();
			log.debug("annotated Element: {}", annotatedElement);

			Optional<DisabledOnServerNotListening> optionalDisabledOnServerNotListening =
					AnnotationSupport.findAnnotation(annotatedElement, DisabledOnServerNotListening.class);

			if (optionalDisabledOnServerNotListening.isPresent())
			{
				DisabledOnServerNotListening disabledOnServerNotListening = optionalDisabledOnServerNotListening.get();

				String propertyNameHost = disabledOnServerNotListening.propertyNameHost();
				String propertyNamePort = disabledOnServerNotListening.propertyNamePort();
				log.debug("\nproperty name host: {}\nproperty name port: {}", propertyNameHost, propertyNamePort);

				String propertyValueHost =
						ConfigProvider
								.getConfig()
								.getOptionalValue(propertyNameHost, String .class)
								.orElse("missing value for " + propertyNameHost + " in microprofile-config.properties");
				int    propertyValuePort =
						ConfigProvider
								.getConfig()
								.getOptionalValue(propertyNamePort, Integer.class)
								.orElse(-1);

				HostPortConfigExtension hostPortConfigExtension =
						HostPortConfigExtension
								.builder()
										.host(propertyValueHost)
										.port(propertyValuePort)
								.build();

				try
				{
					if (IO.isListening(hostPortConfigExtension.getHost(), hostPortConfigExtension.getPort()))
					{
						String message = MessageFormat.format(
								"server at \"{0}:{1,number,#}\" is listening -> execution enabled",
								hostPortConfigExtension.getHost(),
								hostPortConfigExtension.getPort());
						log.debug(message);
						return ConditionEvaluationResult.enabled(message);
					}
					else
					{
						String message = MessageFormat.format(
								"server at \"{0}:{1,number,#}\" is not listening -> execution disabled",
								hostPortConfigExtension.getHost(),
								hostPortConfigExtension.getPort());
						log.debug(message);
						return ConditionEvaluationResult.disabled(message);
					}
				}
				catch (UnknownHostException e)
				{
					String message = MessageFormat.format(
							"host \"{0}\" is not known -> execution disabled",
							hostPortConfigExtension.getHost());
					log.debug(message);
					return ConditionEvaluationResult.disabled(message);
				}
			}
			else
			{
				String message = "annotation not found -> execution enabled";
				log.debug(message);
				return ConditionEvaluationResult.enabled(message);
			}
		}
		else
		{
			String message = "no annotated element";
			log.debug(message);
			return ConditionEvaluationResult.disabled(message);
		}
	}
}
