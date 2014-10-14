package me.enat.coffee.mackern;

import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

import java.io.File;
import java.lang.*;
import java.lang.Class;
import java.lang.ClassLoader;
import java.lang.ClassNotFoundException;
import java.lang.Exception;
import java.lang.IllegalAccessException;
import java.lang.IllegalArgumentException;
import java.lang.NoSuchMethodException;
import java.lang.Object;
import java.lang.SecurityException;
import java.lang.String;
import java.lang.System;
import java.lang.Throwable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.List;


public class Mackern {

    private static AboutEventHandler aboutEventHandler;
    private static QuitEventHandler quitEventHandler;
    private static OpenURIEventHandler openURIEventHandler;
    private static OpenFileEventHandler openFileEventHandler;
    private static NoMacEvent noMacEvent = new NoMacEvent() {
        @Override
        public void noMacClassesFound(Exception e) {
            System.out.println("No Mac Classes Found");
        }
    };


    public static void setOpenURIEventHandler(final OpenURIEventHandler openURIEventHandler) {
        Mackern.openURIEventHandler = openURIEventHandler;
        try {
            final InvocationHandler ih = new InvocationHandler() {

                @Override
                public java.lang.Object invoke(Object proxy, Method method, Object[] args)
                        throws java.lang.Throwable {
                    Class<?> class_openFilesEvent = getEventClassWithName(EVENT_TYPES.OPEN_URI_EVENT.eventClassname);
                    Method filesMethod = class_openFilesEvent.getMethod("getURI");
                    URI uri = (URI) filesMethod.invoke(args[0]);
                    openURIEventHandler.recivedOpenURIEvent(uri);
                    return null;
                }
            };

            setInvocationHandler(ih, EVENT_TYPES.OPEN_FILES_EVENT);

        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            noMacEvent.noMacClassesFound(e);
        }
    }

    public static void setOpenFileEventHandler(final OpenFileEventHandler openFileEventHandler) {
        Mackern.openFileEventHandler = openFileEventHandler;
        try {
            final InvocationHandler ih = new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    Class<?> class_openFilesEvent = getEventClassWithName(EVENT_TYPES.OPEN_FILES_EVENT.eventClassname);
                    Method filesMethod = class_openFilesEvent.getMethod("getFiles");
                    List<File> files = (List<File>) filesMethod.invoke(args[0]);
                    openFileEventHandler.recivedOpenFileEvent(files);
                    return null;
                }
            };

            setInvocationHandler(ih, EVENT_TYPES.OPEN_FILES_EVENT);

        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            noMacEvent.noMacClassesFound(e);
        }
    }

    private static void setInvocationHandler(InvocationHandler ih, EVENT_TYPES type) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> class_application = getApplicationClass();
        Object instance_application = getApplication();

        Object proxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[]{Class.forName(type.handlerClassName)}, ih);

        Method method_setOpenFileHandler = class_application.getMethod(type.setterMethod,
                Class.forName(type.handlerClassName));

        method_setOpenFileHandler.invoke(instance_application, proxy);
    }

    public static void setAboutEventHandler(final AboutEventHandler aboutEventHandler) {
        Mackern.aboutEventHandler = aboutEventHandler;
        try {
            final InvocationHandler ih = new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    Class<?> class_openFilesEvent = getEventClassWithName(EVENT_TYPES.ABOUT_EVENT.eventClassname);
                    aboutEventHandler.recivedAboutEvent();
                    return null;
                }
            };

            setInvocationHandler(ih, EVENT_TYPES.OPEN_FILES_EVENT);

        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            noMacEvent.noMacClassesFound(e);
        }
    }

    private static Object getApplication() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> class_application = getApplicationClass();

        Method method_getApplication = class_application.getMethod("getApplication");
        Object instance_application = method_getApplication.invoke(class_application);
        return instance_application;
    }

    private static Class<?> getApplicationClass() throws ClassNotFoundException {
        return Class.forName("com.apple.eawt.Application");
    }

    private static Class<?> getEventClassWithName(String name) throws ClassNotFoundException {
        Class<?> class_appEvent = Class.forName("com.apple.eawt.AppEvent");
        Class<?>[] classesOf_class_appEvent = class_appEvent.getDeclaredClasses();
        for (Class<?> currentClass : classesOf_class_appEvent) {
            if (currentClass.getSimpleName().equals(name)) {
                return currentClass;
            }
        }
        return null;
    }

    public static void setNoMacEvent(NoMacEvent noMacEvent) {
        Mackern.noMacEvent = noMacEvent;
    }

    private enum EVENT_TYPES {
        OPEN_FILES_EVENT("OpenFilesEvent", "setOpenFileHandler", "com.apple.eawt.OpenFilesHandler"),
        OPEN_URI_EVENT  ("OpenURIEvent"  , "setOpenURIHandler" , "com.apple.eawt.OpenURIHandler"),
        ABOUT_EVENT     ("AboutEvent"    , "setAboutHandler"   , "com.apple.eawt.Abouthandler");

        public final String eventClassname;
        public final String setterMethod;
        public final String handlerClassName;

        private EVENT_TYPES(String i, String j, String k) {
            eventClassname = i;
            setterMethod = j;
            handlerClassName = k;
        }
    }

    public interface OpenFileEventHandler {
        public void recivedOpenFileEvent(List<File> files);
    }

    public interface OpenURIEventHandler {
        public void recivedOpenURIEvent(URI uri);
    }


    public interface QuitEventHandler {
        public void recivedQuitEvent(/* TODO: INSERT EVENT DATA */);
    }

    public interface AboutEventHandler {
        public void recivedAboutEvent();
    }

    public interface NoMacEvent {
        public void noMacClassesFound(Exception e);
    }



    //public static final String APP_REOPENED_EVENT = "AppReOpenedEvent";
    //public static final String APP_FOREGROUND_EVENT = "AppForegroundEvent";
    //public static final String APP_HIDDEN_EVENT = "AppHiddenEvent";
    //public static final String USER_SESSION_EVENT = "UserSessionEvent";
    //public static final String SCREEN_SLEEP_EVENT = "ScreenSleepEvent";
    //public static final String SYSTEM_SLEEP_EVENT = "SystemSleepEvent";
    //public static final String OPEN_FILES_EVENT = "OpenFilesEvent";
    //public static final String FILES_EVENT = "FilesEvent";
    //public static final String OPEN_URI_EVENT = "OpenURIEvent";
    //public static final String ABOUT_EVENT = "AboutEvent";
    //public static final String PREFERENCES_EVENT = "PreferencesEvent";
    //public static final String QUIT_EVENT = "QuitEvent";

}
