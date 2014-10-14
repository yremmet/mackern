package me.enat.coffee.mackern;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.List;


public class Mackern {


    AboutEventHandler aboutEventHandler;
    QuitEventHandler quitEventHandler;
    OpenURIEventHandler openURIEventHandler;
    OpenFileEventHandler openFileEventHandler;
    NoMacEvent noMacEvent;

    public Mackern() {
        this.noMacEvent = new NoMacEvent() {
            @Override
            public void noMacClassesFound(Exception e) {
                System.out.println("No Mac Classes Found");
            }
        };
    }

    public void setOpenURIEventHandler(final OpenURIEventHandler openURIEventHandler) {
        this.openURIEventHandler = openURIEventHandler;
        try {
            final InvocationHandler ih = new InvocationHandler() {

                @Override
                public java.lang.Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    Class<?> class_openFilesEvent = getEventClassWithName(EVENT_TYPES.OPEN_URI_EVENT.handlerClassName);
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

    public void setOpenFileEventHandler(final OpenFileEventHandler openFileEventHandler) {
        this.openFileEventHandler = openFileEventHandler;
        try {
            final InvocationHandler ih = new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    Class<?> class_openFilesEvent = getEventClassWithName(EVENT_TYPES.OPEN_FILES_EVENT.handlerClassName);
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

    private void setInvocationHandler(InvocationHandler ih, EVENT_TYPES type) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> class_application = getApplicationClass();
        Object instance_application = getApplication();

        Object proxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[]{Class.forName(type.handlerClassName)}, ih);

        Method method_setOpenFileHandler = class_application.getMethod(type.setterMethod,
                Class.forName(type.handlerClassName));

        method_setOpenFileHandler.invoke(instance_application, proxy);
    }

    private Object getApplication() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> class_application = getApplicationClass();

        Method method_getApplication = class_application.getMethod("getApplication");
        Object instance_application = method_getApplication.invoke(class_application);
        return instance_application;
    }

    private Class<?> getApplicationClass() throws ClassNotFoundException {
        return Class.forName("com.apple.eawt.Application");
    }

    private Class<?> getEventClassWithName(String name) throws ClassNotFoundException {
        Class<?> class_appEvent = Class.forName("com.apple.eawt.AppEvent");
        Class<?>[] classesOf_class_appEvent = class_appEvent.getDeclaredClasses();
        for (Class<?> currentClass : classesOf_class_appEvent) {
            if (currentClass.getSimpleName().equals(name)) {
                return currentClass;
            }
        }
        return null;
    }

    public void setNoMacEvent(NoMacEvent noMacEvent) {
        this.noMacEvent = noMacEvent;
    }


    private enum EVENT_TYPES {
        OPEN_FILES_EVENT("OpenFilesEvent", "setOpenFileHandler", "com.apple.eawt.OpenFilesHandler"),
        OPEN_URI_EVENT("OpenURIEvent", "setOpenURIHandler", "com.apple.eawt.OpenURIHandler");

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



    /*
              Application.getApplication().setOpenURIHandler(new OpenURIHandler() {
                    @Override
                    public void openURI(AppEvent.OpenURIEvent openURIEvent) {
                        openURIEvent.getURI()
                    }
                }););

                Application.getApplication().setAboutHandler();
     */
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