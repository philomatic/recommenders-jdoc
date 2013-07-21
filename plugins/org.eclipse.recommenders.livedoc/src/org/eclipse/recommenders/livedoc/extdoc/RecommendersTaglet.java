package org.eclipse.recommenders.livedoc.extdoc;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.internal.toolkit.taglets.Taglet;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletOutput;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletWriter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.recommenders.livedoc.extdoc.ClassOverridesWriter;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class RecommendersTaglet implements Taglet {
  private final List<ClassOverridesWriter> docAppenders = new Function0<List<ClassOverridesWriter>>() {
    public List<ClassOverridesWriter> apply() {
      ClassOverridesWriter _classOverridesWriter = new ClassOverridesWriter();
      return Collections.<ClassOverridesWriter>unmodifiableList(Lists.<ClassOverridesWriter>newArrayList(_classOverridesWriter));
    }
  }.apply();
  
  public static Object register(final Map tagletMap) {
    Object _xblockexpression = null;
    {
      RecommendersTaglet _recommendersTaglet = new RecommendersTaglet();
      final RecommendersTaglet taglet = _recommendersTaglet;
      String _name = taglet.getName();
      Object _get = tagletMap.get(_name);
      final Taglet t = ((Taglet) _get);
      boolean _notEquals = (!Objects.equal(t, null));
      if (_notEquals) {
        String _name_1 = taglet.getName();
        tagletMap.remove(_name_1);
      }
      String _name_2 = taglet.getName();
      Object _put = tagletMap.put(_name_2, taglet);
      _xblockexpression = (_put);
    }
    return _xblockexpression;
  }
  
  public String getName() {
    return "rec";
  }
  
  public boolean inConstructor() {
    return true;
  }
  
  public boolean inField() {
    return true;
  }
  
  public boolean inMethod() {
    return true;
  }
  
  public boolean inOverview() {
    return true;
  }
  
  public boolean inPackage() {
    return true;
  }
  
  public boolean inType() {
    return true;
  }
  
  public boolean isInlineTag() {
    return false;
  }
  
  public TagletOutput getTagletOutput(final Tag tag, final TagletWriter writer) throws IllegalArgumentException {
    UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException("Auto-generated function stub");
    throw _unsupportedOperationException;
  }
  
  public TagletOutput getTagletOutput(final Doc holder, final TagletWriter writer) throws IllegalArgumentException {
    StringBuilder _stringBuilder = new StringBuilder();
    final StringBuilder sb = _stringBuilder;
    final Procedure1<ClassOverridesWriter> _function = new Procedure1<ClassOverridesWriter>() {
        public void apply(final ClassOverridesWriter it) {
          sb.append("<div class=\"section\">");
          CharSequence _render = it.render(holder);
          sb.append(_render);
          sb.append("</div>");
        }
      };
    IterableExtensions.<ClassOverridesWriter>forEach(this.docAppenders, _function);
    final TagletOutput res = writer.getOutputInstance();
    String _string = sb.toString();
    res.setOutput(_string);
    return res;
  }
  
  public String toString(final Tag tag) {
    UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException("TODO: auto-generated method stub");
    throw _unsupportedOperationException;
  }
  
  public String toString(final Tag[] tags) {
    UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException("TODO: auto-generated method stub");
    throw _unsupportedOperationException;
  }
}
