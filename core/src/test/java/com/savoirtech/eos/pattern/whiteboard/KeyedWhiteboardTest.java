package com.savoirtech.eos.pattern.whiteboard;

import com.savoirtech.eos.test.MockObjectTestCase;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.osgi.framework.*;

import static org.mockito.Mockito.*;

public class KeyedWhiteboardTest extends MockObjectTestCase {

    @Mock
    private BundleContext bundleContext;

    @Mock
    private Filter filter;

    @Mock
    private ServiceReference<HelloService> serviceReference;

    @Mock
    private HelloService service;

    @Mock
    private Bundle bundle;

    @Captor
    private ArgumentCaptor<ServiceListener> listenerCaptor;

    @Test
    public void testAddingService() throws Exception {

        KeyedWhiteboard<String,HelloService> whiteboard = new KeyedWhiteboard<>(bundleContext, HelloService.class, (svc, props) -> svc.getLanguage());
        String filterSpec = String.format("(objectClass=%s)", HelloService.class.getName());
        when(bundleContext.createFilter(filterSpec)).thenReturn(filter);

        verify(bundleContext).createFilter(filterSpec);
        verify(bundleContext).addServiceListener(listenerCaptor.capture(), eq(filterSpec));
        verify(bundleContext).getAllServiceReferences(HelloService.class.getName(), null);
        verifyNoMoreInteractions(bundleContext);

        ServiceListener listener = listenerCaptor.getValue();

        when(bundleContext.getService(serviceReference)).thenReturn(service);
        when(service.getLanguage()).thenReturn("english");
        when(serviceReference.getBundle()).thenReturn(bundle);
        when(bundle.getSymbolicName()).thenReturn("test");
        when(serviceReference.getProperty(Constants.SERVICE_ID)).thenReturn(1L);
        listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,serviceReference));
        assertEquals(1, whiteboard.getServiceCount());
        assertEquals(service, whiteboard.getService("english"));
    }

    @Test
    public void testAddingServiceWithNullKey() throws Exception {

        String filterSpec = String.format("(objectClass=%s)", HelloService.class.getName());
        when(bundleContext.createFilter(filterSpec)).thenReturn(filter);
        when(bundleContext.getService(serviceReference)).thenReturn(service);
        when(serviceReference.getBundle()).thenReturn(bundle);
        when(bundle.getSymbolicName()).thenReturn("test");
        when(serviceReference.getProperty(Constants.SERVICE_ID)).thenReturn(1L);
        doNothing().when(bundleContext).addServiceListener(listenerCaptor.capture(), eq(filterSpec));

        KeyedWhiteboard<String,HelloService> whiteboard = new KeyedWhiteboard<>(bundleContext, HelloService.class, (svc, props) -> null);

        ServiceListener listener = listenerCaptor.getValue();

        verify(bundleContext).createFilter(filterSpec);
        verify(bundleContext).addServiceListener(listenerCaptor.capture(), eq(filterSpec));
        verify(bundleContext).getAllServiceReferences(HelloService.class.getName(), null);

        listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,serviceReference));

        verify(bundleContext).getService(serviceReference);
        verify(bundleContext).ungetService(serviceReference);
        verifyNoMoreInteractions(bundleContext);

        assertEquals(0, whiteboard.getServiceCount());
    }

    @Test
    public void testModifiedService() throws Exception {
        String filterSpec = String.format("(objectClass=%s)", HelloService.class.getName());
        when(bundleContext.createFilter(filterSpec)).thenReturn(filter);
        doNothing().when(bundleContext).addServiceListener(listenerCaptor.capture(), eq(filterSpec));

        KeyedWhiteboard<String,HelloService> whiteboard = new KeyedWhiteboard<>(bundleContext, HelloService.class, (svc, props) -> svc.getLanguage());

        ServiceListener listener = listenerCaptor.getValue();

        when(bundleContext.getService(serviceReference)).thenReturn(service);
        when(service.getLanguage()).thenReturn("english");
        when(serviceReference.getBundle()).thenReturn(bundle);
        when(bundle.getSymbolicName()).thenReturn("test");
        when(serviceReference.getProperty(Constants.SERVICE_ID)).thenReturn(1L);

        listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,serviceReference));

        when(service.getLanguage()).thenReturn("spanish");
        listener.serviceChanged(new ServiceEvent(ServiceEvent.MODIFIED,serviceReference));
        assertEquals(1, whiteboard.getServiceCount());
        assertEquals(service, whiteboard.getService("spanish"));
    }

    @Test
    public void testModifiedServiceWhenKeyInvalid() throws Exception {
        String filterSpec = String.format("(objectClass=%s)", HelloService.class.getName());
        when(bundleContext.createFilter(filterSpec)).thenReturn(filter);
        doNothing().when(bundleContext).addServiceListener(listenerCaptor.capture(), eq(filterSpec));

        KeyedWhiteboard<String,HelloService> whiteboard = new KeyedWhiteboard<>(bundleContext, HelloService.class, (svc, props) -> svc.getLanguage());

        ServiceListener listener = listenerCaptor.getValue();

        when(bundleContext.getService(serviceReference)).thenReturn(service);
        when(service.getLanguage()).thenReturn("english");
        when(serviceReference.getBundle()).thenReturn(bundle);
        when(bundle.getSymbolicName()).thenReturn("test");
        when(serviceReference.getProperty(Constants.SERVICE_ID)).thenReturn(1L);

        listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,serviceReference));

        when(service.getLanguage()).thenReturn(null);
        listener.serviceChanged(new ServiceEvent(ServiceEvent.MODIFIED,serviceReference));
        assertEquals(0, whiteboard.getServiceCount());
        verify(bundleContext).ungetService(serviceReference);
    }

    @Test
    public void testModifiedServiceWhenNotRegistered() throws Exception {
        String filterSpec = String.format("(objectClass=%s)", HelloService.class.getName());
        when(bundleContext.createFilter(filterSpec)).thenReturn(filter);
        doNothing().when(bundleContext).addServiceListener(listenerCaptor.capture(), eq(filterSpec));

        KeyedWhiteboard<String,HelloService> whiteboard = new KeyedWhiteboard<>(bundleContext, HelloService.class, (svc, props) -> svc.getLanguage());

        ServiceListener listener = listenerCaptor.getValue();

        when(bundleContext.getService(serviceReference)).thenReturn(service);
        when(service.getLanguage()).thenReturn("english");
        when(serviceReference.getBundle()).thenReturn(bundle);
        when(bundle.getSymbolicName()).thenReturn("test");
        when(serviceReference.getProperty(Constants.SERVICE_ID)).thenReturn(1L);

        listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,serviceReference));

        when(service.getLanguage()).thenReturn("spanish");
        when(serviceReference.getProperty(Constants.SERVICE_ID)).thenReturn(2L);
        listener.serviceChanged(new ServiceEvent(ServiceEvent.MODIFIED,serviceReference));
        assertEquals(1, whiteboard.getServiceCount());
        verify(bundleContext).ungetService(serviceReference);
    }

    @Test
    public void testClose() throws Exception {
        KeyedWhiteboard<String,HelloService> whiteboard = new KeyedWhiteboard<>(bundleContext, HelloService.class, (svc, props) -> svc.getLanguage());
        String filterSpec = String.format("(objectClass=%s)", HelloService.class.getName());
        when(bundleContext.createFilter(filterSpec)).thenReturn(filter);

        verify(bundleContext).createFilter(filterSpec);
        verify(bundleContext).addServiceListener(listenerCaptor.capture(), eq(filterSpec));
        verify(bundleContext).getAllServiceReferences(HelloService.class.getName(), null);
        verifyNoMoreInteractions(bundleContext);

        ServiceListener listener = listenerCaptor.getValue();

        when(bundleContext.getService(serviceReference)).thenReturn(service);
        when(service.getLanguage()).thenReturn("english");
        when(serviceReference.getBundle()).thenReturn(bundle);
        when(bundle.getSymbolicName()).thenReturn("test");
        when(serviceReference.getProperty(Constants.SERVICE_ID)).thenReturn(1L);
        whiteboard.close();
        verify(bundleContext).removeServiceListener(listener);
        verifyNoMoreInteractions(bundleContext);
    }

    interface HelloService {
        String sayHello(String name);
        String getLanguage();
    }

}