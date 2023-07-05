// AnalysisInputLocation<JavaSootClass> inputLocation = new
// PathBasedAnalysisInputLocation(path);
// p(inputLocation.getClass());

// Previous debugging
// try {
// Field privateField =
// JavaSourcePathAnalysisInputLocation.class.getDeclaredField("classProvider");
// privateField.setAccessible(true);
// JavaSourcePathAnalysisInputLocation loc =
// (JavaSourcePathAnalysisInputLocation) inputLocation;
// WalaJavaClassProvider wjp = (WalaJavaClassProvider) privateField.get(loc);
// p(wjp);
// Method method[] = WalaJavaClassProvider.class.getDeclaredMethods();
// Method m = null;
// for (int i = 0; i < method.length; i++) {
// String meth = new String(method[i].toString());
// p(meth);
// if (meth.contains("iterateWalaClasses")) {
// m = method[i];
// break;
// }
// }
// // Method m = WalaJavaClassProvider.class.getDeclaredMethod(mname);
// m.setAccessible(true);
// Iterator<IClass> it = (Iterator<IClass>) m.invoke(wjp);
// while (it.hasNext()) {
// JavaSourceLoaderImpl.JavaClass walaClass = (JavaSourceLoaderImpl.JavaClass)
// it.next();
// // p(walaClass);
// for (IMethod walaMethod : walaClass.getDeclaredMethods()) {
// ConcreteJavaMethod walam = (ConcreteJavaMethod) walaMethod;
// if (!walam.toString().contains("some"))
// continue;

// p(walaMethod);
// // p(walaMethod.getClass());

// DebuggingInformation dinfo = walam.debugInfo();
// SymbolTable st = walam.symbolTable();
// // p("!!! " + st.isParameter(1));
// // p("!!! " + st.isParameter(3));
// String[][] names = dinfo.getSourceNamesForValues();
// for (int i = 0; i < names.length; i++) {
// String[] tmp = names[i];
// if (tmp.length != 0) {
// // p(i);
// for (String s : tmp) {
// // p(s);
// }
// }
// }
// // p("-----");
// // for (int i = 0; i <= st.getMaxValueNumber(); i++) {
// // p(i + ": " + st.getValueString(i));
// // if (i < 1)
// // continue;
// // p(i + ": " + st.getValue(i));
// // }
// // AbstractCFG<?, ?> cfg = walam.cfg();
// // for (SSAInstruction inst : (SSAInstruction[]) cfg.getInstructions()) {
// // p(inst);
// // }
// }
// }

// } catch (NoSuchFieldException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// } catch (SecurityException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// } catch (IllegalArgumentException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// } catch (IllegalAccessException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// } catch (InvocationTargetException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }