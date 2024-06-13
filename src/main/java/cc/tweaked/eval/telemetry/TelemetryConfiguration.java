package cc.tweaked.eval.telemetry;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.opentelemetry.semconv.ServiceAttributes.SERVICE_NAME;

public class TelemetryConfiguration {
    public static final String NAME = "cct-eval";

    private static Tracer tracer;

    public static void setup() {
        Resource resource = Resource.getDefault().merge(Resource.create(Attributes.builder()
            .put(SERVICE_NAME, NAME)
            .build()
        ));

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
            .setResource(resource)
            .build();

        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .setResource(resource)
            .registerMetricReader(
                PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build())
                    .setInterval(15, TimeUnit.SECONDS)
                    .build())
            .build();

        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setPropagators(ContextPropagators.create(TextMapPropagator.composite(List.of(
                JaegerPropagator.getInstance(),
                W3CTraceContextPropagator.getInstance()
            ))))
            .buildAndRegisterGlobal();

        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

        tracer = GlobalOpenTelemetry.getTracer(NAME);
    }

    public static Tracer tracer() {
        return Objects.requireNonNull(tracer);
    }
}
