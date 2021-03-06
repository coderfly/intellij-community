/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.psi;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.TypeConversionUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author ven
 */

// Intersection types arise in the process of computing lub.
public class PsiIntersectionType extends PsiType {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.PsiIntersectionType");
  private final PsiType[] myConjuncts;

  private PsiIntersectionType(@NotNull PsiType[] conjuncts) {
    super(PsiAnnotation.EMPTY_ARRAY);//todo
    LOG.assertTrue(conjuncts.length != 0);
    LOG.assertTrue(conjuncts.length > 1);
    myConjuncts = conjuncts;
  }

  private static PsiType[] flattenAndRemoveDuplicates(PsiType[] conjuncts) {
    try {
      Set<PsiType> flattened = flatten(conjuncts, new THashSet<PsiType>());
      return flattened.toArray(new PsiType[flattened.size()]);
    }
    catch (NoSuchElementException e) {
      throw new RuntimeException(Arrays.toString(conjuncts), e);
    }
  }

  private static Set<PsiType> flatten(PsiType[] conjuncts, Set<PsiType> types) {
    for (PsiType conjunct : conjuncts) {
      if (conjunct instanceof PsiIntersectionType) {
        PsiIntersectionType type = (PsiIntersectionType)conjunct;
        flatten(type.getConjuncts(), types);
      }
      else {
        types.add(conjunct);
      }
    }
    if (types.size() > 1) {
      PsiType[] array = types.toArray(new PsiType[types.size()]);
      for (Iterator<PsiType> iterator = types.iterator(); iterator.hasNext();) {
        PsiType type = iterator.next();

        for (PsiType existing : array) {
          if (type != existing) {
            final boolean allowUncheckedConversion = type instanceof PsiClassType && ((PsiClassType)type).isRaw();
            if (TypeConversionUtil.isAssignable(type, existing, allowUncheckedConversion)) {
              iterator.remove();
              break;
            }
          }
        }
      }
      if (types.isEmpty()) {
        types.add(array[0]);
      }
    }
    return types;
  }

  @NotNull
  public PsiType[] getConjuncts() {
    return myConjuncts;
  }

  @Override
  public String getPresentableText() {
    return myConjuncts[0].getPresentableText();
  }

  @Override
  public String getCanonicalText() {
    return myConjuncts[0].getCanonicalText();
  }

  @Override
  public String getInternalCanonicalText() {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < myConjuncts.length; i++) {
      buffer.append(myConjuncts[i].getInternalCanonicalText());
      if (i < myConjuncts.length - 1) buffer.append(" & ");
    }
    return buffer.toString();
  }

  @Override
  public boolean isValid() {
    for (PsiType conjunct : myConjuncts) {
      if (!conjunct.isValid()) return false;
    }
    return true;
  }

  @Override
  public boolean equalsToText(String text) {
    return false;
  }

  @Override
  public <A> A accept(@NotNull PsiTypeVisitor<A> visitor) {
    return myConjuncts[0].accept(visitor);
  }

  @Override
  public GlobalSearchScope getResolveScope() {
    return myConjuncts[0].getResolveScope();
  }

  @Override
  @NotNull
  public PsiType[] getSuperTypes() {
    return myConjuncts;
  }

  public static PsiType createIntersection(PsiType... conjuncts) {
    LOG.assertTrue(conjuncts.length >= 1);
    conjuncts = flattenAndRemoveDuplicates(conjuncts);
    if (conjuncts.length == 1) return conjuncts[0];
    return new PsiIntersectionType(conjuncts);
  }

  public PsiType getRepresentative() {
    return myConjuncts[0];
  }

  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof PsiIntersectionType)) return false;
    final PsiType[] first = getConjuncts();
    final PsiType[] second = ((PsiIntersectionType)obj).getConjuncts();
    if (first.length != second.length) return false;
    //positional equality
    for (int i = 0; i < first.length; i++) {
      if (!first[i].equals(second[i])) return false;
    }

    return true;
  }

  public int hashCode() {
    return myConjuncts[0].hashCode();
  }

  @Override
  public String toString() {
    String s = "PsiIntersectionType: ";
    for (PsiType conjunct : myConjuncts) {
      s += conjunct.getPresentableText() +", ";
    }
    return s;
  }
}
