package com.recepcion.recepcion.graphql.config;

import graphql.scalars.ExtendedScalars;
import graphql.schema.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
//import java.util.Map;
import java.util.UUID;

/**
 * Configuración de Scalars personalizados para GraphQL
 */
@Configuration
public class GraphQLScalarConfig {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                // Scalars de Extended Scalars
                .scalar(ExtendedScalars.GraphQLLong)
                .scalar(ExtendedScalars.GraphQLBigDecimal)
                .scalar(ExtendedScalars.Json)
                // Scalars personalizados
                .scalar(uuidScalar())
                .scalar(dateTimeScalar());
    }

    /**
     * Scalar para UUID
     */
    private GraphQLScalarType uuidScalar() {
        return GraphQLScalarType.newScalar()
                .name("UUID")
                .description("UUID scalar type")
                .coercing(new Coercing<UUID, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        if (dataFetcherResult instanceof UUID) {
                            return dataFetcherResult.toString();
                        }
                        throw new CoercingSerializeException("Expected UUID type");
                    }

                    @Override
                    public UUID parseValue(Object input) throws CoercingParseValueException {
                        try {
                            if (input instanceof String) {
                                return UUID.fromString((String) input);
                            }
                            throw new CoercingParseValueException("Expected String");
                        } catch (IllegalArgumentException e) {
                            throw new CoercingParseValueException("Invalid UUID format", e);
                        }
                    }

                    @Override
                    public UUID parseLiteral(Object input) throws CoercingParseLiteralException {
                        if (input instanceof graphql.language.StringValue) {
                            try {
                                return UUID.fromString(((graphql.language.StringValue) input).getValue());
                            } catch (IllegalArgumentException e) {
                                throw new CoercingParseLiteralException("Invalid UUID format", e);
                            }
                        }
                        throw new CoercingParseLiteralException("Expected StringValue");
                    }

                    @Override
                    public graphql.language.Value<?> valueToLiteral(Object input) {
                        return new graphql.language.StringValue(input.toString());
                    }
                })
                .build();
    }

    /**
     * Scalar para DateTime (LocalDateTime)
     */
    private GraphQLScalarType dateTimeScalar() {
        return GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("DateTime scalar type (ISO-8601)")
                .coercing(new Coercing<LocalDateTime, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        if (dataFetcherResult instanceof LocalDateTime) {
                            return ((LocalDateTime) dataFetcherResult).format(DATE_TIME_FORMATTER);
                        }
                        throw new CoercingSerializeException("Expected LocalDateTime type");
                    }

                    @Override
                    public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
                        try {
                            if (input instanceof String) {
                                return LocalDateTime.parse((String) input, DATE_TIME_FORMATTER);
                            }
                            throw new CoercingParseValueException("Expected String");
                        } catch (Exception e) {
                            throw new CoercingParseValueException("Invalid DateTime format. Expected ISO-8601", e);
                        }
                    }

                    @Override
                    public LocalDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
                        if (input instanceof graphql.language.StringValue) {
                            try {
                                return LocalDateTime.parse(
                                        ((graphql.language.StringValue) input).getValue(),
                                        DATE_TIME_FORMATTER
                                );
                            } catch (Exception e) {
                                throw new CoercingParseLiteralException("Invalid DateTime format. Expected ISO-8601", e);
                            }
                        }
                        throw new CoercingParseLiteralException("Expected StringValue");
                    }

                    @Override
                    public graphql.language.Value<?> valueToLiteral(Object input) {
                        if (input instanceof LocalDateTime) {
                            return new graphql.language.StringValue(
                                    ((LocalDateTime) input).format(DATE_TIME_FORMATTER)
                            );
                        }
                        return new graphql.language.StringValue(input.toString());
                    }
                })
                .build();
    }
}
