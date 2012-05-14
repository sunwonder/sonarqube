/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.server.ui;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sonar.api.utils.AnnotationUtils;
import org.sonar.api.web.DefaultTab;
import org.sonar.api.web.Description;
import org.sonar.api.web.GwtPage;
import org.sonar.api.web.NavigationSection;
import org.sonar.api.web.RequiredMeasures;
import org.sonar.api.web.ResourceLanguage;
import org.sonar.api.web.ResourceQualifier;
import org.sonar.api.web.ResourceScope;
import org.sonar.api.web.UserRole;
import org.sonar.api.web.View;
import org.sonar.api.web.Widget;
import org.sonar.api.web.WidgetCategory;
import org.sonar.api.web.WidgetLayout;
import org.sonar.api.web.WidgetLayoutType;
import org.sonar.api.web.WidgetProperties;
import org.sonar.api.web.WidgetProperty;
import org.sonar.api.web.WidgetScope;

import java.util.Collection;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class ViewProxy<V extends View> implements Comparable<ViewProxy> {

  private V view;
  private String[] sections = {NavigationSection.HOME};
  private String[] userRoles = {};
  private String[] resourceScopes = {};
  private String[] resourceQualifiers = {};
  private String[] resourceLanguages = {};
  private String[] defaultForMetrics = {};
  private String description = "";
  private Map<String, WidgetProperty> widgetPropertiesByKey = Maps.newHashMap();
  private String[] widgetCategories = {};
  private WidgetLayoutType widgetLayout = WidgetLayoutType.DEFAULT;
  private boolean isDefaultTab = false;
  private boolean isWidget = false;
  private boolean isGlobal = false;
  private String[] mandatoryMeasures = {};
  private String[] needOneOfMeasures = {};

  public ViewProxy(final V view) {
    this.view = view;

    initUserRoles(view);
    initSections(view);
    initResourceScope(view);
    initResourceQualifier(view);
    initResourceLanguage(view);
    initDefaultTabInfo(view);
    initDescription(view);
    initWidgetProperties(view);
    initWidgetCategory(view);
    initWidgetLayout(view);
    initWidgetGlobal(view);
    initRequiredMeasures(view);

    isWidget = (view instanceof Widget);
  }

  private void initRequiredMeasures(V view) {
    RequiredMeasures requiredMeasuresAnnotation = AnnotationUtils.getClassAnnotation(view, RequiredMeasures.class);
    if (requiredMeasuresAnnotation != null) {
      mandatoryMeasures = requiredMeasuresAnnotation.allOf();
      needOneOfMeasures = requiredMeasuresAnnotation.anyOf();
    }
  }

  private void initWidgetLayout(final V view) {
    WidgetLayout layoutAnnotation = AnnotationUtils.getClassAnnotation(view, WidgetLayout.class);
    if (layoutAnnotation != null) {
      widgetLayout = layoutAnnotation.value();
    }
  }

  private void initWidgetCategory(final V view) {
    WidgetCategory categAnnotation = AnnotationUtils.getClassAnnotation(view, WidgetCategory.class);
    if (categAnnotation != null) {
      widgetCategories = categAnnotation.value();
    }
  }

  private void initWidgetGlobal(V view) {
    WidgetScope scopeAnnotation = AnnotationUtils.getClassAnnotation(view, WidgetScope.class);
    if (scopeAnnotation != null) {
      checkValidScope(view, scopeAnnotation);
      isGlobal = ImmutableSet.copyOf(scopeAnnotation.value()).contains("GLOBAL");
    }
  }

  private static <V> void checkValidScope(V view, WidgetScope scopeAnnotation) {
    for (String scope : scopeAnnotation.value()) {
      if (!scope.equals("PROJECT") && !scope.equalsIgnoreCase("GLOBAL")) {
        throw new IllegalArgumentException(String.format("Invalid widget scope %s for widget %s", scope, view.getClass().getSimpleName()));
      }
    }
  }

  private void initWidgetProperties(final V view) {
    WidgetProperties propAnnotation = AnnotationUtils.getClassAnnotation(view, WidgetProperties.class);
    if (propAnnotation != null) {
      for (WidgetProperty property : propAnnotation.value()) {
        widgetPropertiesByKey.put(property.key(), property);
      }
    }
  }

  private void initDescription(final V view) {
    Description descriptionAnnotation = AnnotationUtils.getClassAnnotation(view, Description.class);
    if (descriptionAnnotation != null) {
      description = descriptionAnnotation.value();
    }
  }

  private void initDefaultTabInfo(final V view) {
    DefaultTab defaultTabAnnotation = AnnotationUtils.getClassAnnotation(view, DefaultTab.class);
    if (defaultTabAnnotation != null) {
      if (defaultTabAnnotation.metrics().length == 0) {
        isDefaultTab = true;
        defaultForMetrics = new String[0];

      } else {
        isDefaultTab = false;
        defaultForMetrics = defaultTabAnnotation.metrics();
      }
    }
  }

  private void initResourceLanguage(final V view) {
    ResourceLanguage languageAnnotation = AnnotationUtils.getClassAnnotation(view, ResourceLanguage.class);
    if (languageAnnotation != null) {
      resourceLanguages = languageAnnotation.value();
    }
  }

  private void initResourceQualifier(final V view) {
    ResourceQualifier qualifierAnnotation = AnnotationUtils.getClassAnnotation(view, ResourceQualifier.class);
    if (qualifierAnnotation != null) {
      resourceQualifiers = qualifierAnnotation.value();
    }
  }

  private void initResourceScope(final V view) {
    ResourceScope scopeAnnotation = AnnotationUtils.getClassAnnotation(view, ResourceScope.class);
    if (scopeAnnotation != null) {
      resourceScopes = scopeAnnotation.value();
    }
  }

  private void initSections(final V view) {
    NavigationSection sectionAnnotation = AnnotationUtils.getClassAnnotation(view, NavigationSection.class);
    if (sectionAnnotation != null) {
      sections = sectionAnnotation.value();
    }
  }

  private void initUserRoles(final V view) {
    UserRole userRoleAnnotation = AnnotationUtils.getClassAnnotation(view, UserRole.class);
    if (userRoleAnnotation != null) {
      userRoles = userRoleAnnotation.value();
    }
  }

  public V getTarget() {
    return view;
  }

  public String getId() {
    return view.getId();
  }

  public boolean isController() {
    String id = view.getId();
    return id !=null && id.length()>0 && id.charAt(0)=='/';
  }

  public String getTitle() {
    return view.getTitle();
  }

  public String getDescription() {
    return description;
  }

  public Collection<WidgetProperty> getWidgetProperties() {
    return widgetPropertiesByKey.values();
  }

  public WidgetProperty getWidgetProperty(String propertyKey) {
    return widgetPropertiesByKey.get(propertyKey);
  }

  public String[] getWidgetCategories() {
    return widgetCategories;
  }

  public String[] getSections() {
    return sections;
  }

  public String[] getUserRoles() {
    return userRoles;
  }

  public String[] getResourceScopes() {
    return resourceScopes;
  }

  public String[] getResourceQualifiers() {
    return resourceQualifiers;
  }

  public String[] getResourceLanguages() {
    return resourceLanguages;
  }

  public boolean isDefaultTab() {
    return isDefaultTab;
  }

  public String[] getDefaultTabForMetrics() {
    return defaultForMetrics;
  }

  public boolean supportsMetric(String metricKey) {
    return ArrayUtils.contains(defaultForMetrics, metricKey);
  }
  
  public boolean acceptsAvailableMeasures(String[] availableMeasures) {
    for (String mandatoryMeasure : mandatoryMeasures) {
      if (!ArrayUtils.contains(availableMeasures, mandatoryMeasure)) {
        return false;
      }
    }
    if (needOneOfMeasures.length == 0) {
      return true;
    } else {
      for (String neededMeasure : needOneOfMeasures) {
        if (ArrayUtils.contains(availableMeasures, neededMeasure)) {
          return true;
        }
      }
      return false;
    }
  }

  public boolean isWidget() {
    return isWidget;
  }

  public boolean isGlobal() {
    return isGlobal;
  }

  public boolean isGwt() {
    return view instanceof GwtPage;
  }

  public WidgetLayoutType getWidgetLayout() {
    return widgetLayout;
  }

  public boolean isEditable() {
    return !widgetPropertiesByKey.isEmpty();
  }

  public boolean hasRequiredProperties() {
    boolean requires = false;
    for (WidgetProperty property : getWidgetProperties()) {
      if (!property.optional() && StringUtils.isEmpty(property.defaultValue())) {
        requires = true;
      }
    }
    return requires;
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    ViewProxy rhs = (ViewProxy) obj;
    return new EqualsBuilder()
        .append(getId(), rhs.getId())
        .isEquals();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", view.getId())
        .append("sections", sections)
        .append("userRoles", userRoles)
        .append("scopes", resourceScopes)
        .append("qualifiers", resourceQualifiers)
        .append("languages", resourceLanguages)
        .append("metrics", defaultForMetrics)
        .toString();

  }

  public int compareTo(ViewProxy other) {
    return new CompareToBuilder()
        .append(getTitle(), other.getTitle())
        .append(getId(), other.getId())
        .toComparison();

  }
}
