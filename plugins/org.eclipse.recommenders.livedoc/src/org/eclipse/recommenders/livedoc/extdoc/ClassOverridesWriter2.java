package org.eclipse.recommenders.livedoc.extdoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import org.eclipse.xtend2.lib.StringConcatenation;

@SuppressWarnings("all")
public class ClassOverridesWriter2 /* implements IDocAppender  */{
  public CharSequence render(final Doc doc) {
    CharSequence _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (doc instanceof ClassDoc) {
        final ClassDoc _classDoc = (ClassDoc)doc;
        _matched=true;
        CharSequence _render = this.render(((ClassDoc) _classDoc));
        _switchResult = _render;
      }
    }
    if (!_matched) {
      if (doc instanceof MethodDoc) {
        final MethodDoc _methodDoc = (MethodDoc)doc;
        _matched=true;
        CharSequence _render = this.render(((MethodDoc) _methodDoc));
        _switchResult = _render;
      }
    }
    if (!_matched) {
      if (doc instanceof ConstructorDoc) {
        final ConstructorDoc _constructorDoc = (ConstructorDoc)doc;
        _matched=true;
        CharSequence _render = this.render(((ConstructorDoc) _constructorDoc));
        _switchResult = _render;
      }
    }
    if (!_matched) {
      if (doc instanceof PackageDoc) {
        final PackageDoc _packageDoc = (PackageDoc)doc;
        _matched=true;
        CharSequence _render = this.render(((PackageDoc) _packageDoc));
        _switchResult = _render;
      }
    }
    return _switchResult;
  }
  
  public CharSequence render(final ClassDoc doc) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<b>overrides class doc:</b>");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("<ul>");
    _builder.newLine();
    _builder.append(" \t");
    _builder.append("<li>one - 60%");
    _builder.newLine();
    _builder.append(" \t");
    _builder.append("<li>two - 30%");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("</ul>");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    return _builder;
  }
  
  public CharSequence render(final MethodDoc doc) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<b>override method docs:</b>");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("<ul>");
    _builder.newLine();
    _builder.append(" \t");
    _builder.append("<li>one - 60%");
    _builder.newLine();
    _builder.append(" \t");
    _builder.append("<li>two - 30%");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("</ul>");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    return _builder;
  }
  
  public CharSequence render(final ConstructorDoc doc) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<b>overrides constructor:</b>");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("<ul>");
    _builder.newLine();
    _builder.append(" \t");
    _builder.append("<li>one - 60%");
    _builder.newLine();
    _builder.append(" \t");
    _builder.append("<li>two - 30%");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("</ul>");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    return _builder;
  }
  
  public CharSequence render(final PackageDoc doc) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<b>frequently extended package:</b>");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("<ul>");
    _builder.newLine();
    _builder.append(" \t");
    _builder.append("<li>one - 60%");
    _builder.newLine();
    _builder.append(" \t");
    _builder.append("<li>two - 30%");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("</ul>");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    return _builder;
  }
}
