/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.intellij.psi.util;

import com.intellij.psi.*;
import com.intellij.util.Processor;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class InheritanceUtil {
  private InheritanceUtil() { }

  /** @deprecated Use {@link PsiClass#isInheritor(com.intellij.psi.PsiClass, boolean)} instead (to remove in IDEA 13) */
  public static boolean isInheritor(@NotNull PsiClass candidateClass, @NotNull PsiClass baseClass, boolean checkDeep) {
    return candidateClass.isInheritor(baseClass, checkDeep);
  }

  /**
   * @param aClass     a class to check.
   * @param baseClass  supposed base class.
   * @param checkDeep  true to check deeper than aClass.super (see {@linkplain PsiClass#isInheritor(com.intellij.psi.PsiClass, boolean)}).
   * @return true if aClass is the baseClass or baseClass inheritor
   */
  public static boolean isInheritorOrSelf(@Nullable PsiClass aClass, @Nullable PsiClass baseClass, boolean checkDeep) {
    if (aClass == null || baseClass == null) return false;
    PsiManager manager = aClass.getManager();
    return manager.areElementsEquivalent(baseClass, aClass) || aClass.isInheritor(baseClass, checkDeep);
  }

  /** @deprecated use {@linkplain #isInheritorOrSelf(com.intellij.psi.PsiClass, com.intellij.psi.PsiClass, boolean) (to remove in IDEA 13)} */
  public static boolean isCorrectDescendant(@Nullable PsiClass aClass, @Nullable PsiClass baseClass, boolean checkDeep) {
    return isInheritorOrSelf(aClass, baseClass, checkDeep);
  }

  public static boolean processSupers(@Nullable PsiClass aClass, boolean includeSelf, @NotNull Processor<PsiClass> superProcessor) {
    if (aClass == null) return true;

    if (includeSelf && !superProcessor.process(aClass)) return false;

    return processSupers(aClass, superProcessor, new THashSet<PsiClass>());
  }

  private static boolean processSupers(@NotNull PsiClass aClass, @NotNull Processor<PsiClass> superProcessor, @NotNull Set<PsiClass> visited) {
    if (!visited.add(aClass)) return true;

    for (final PsiClass intf : aClass.getInterfaces()) {
      if (!superProcessor.process(intf) || !processSupers(intf, superProcessor, visited)) return false;
    }
    final PsiClass superClass = aClass.getSuperClass();
    if (superClass != null) {
      if (!superProcessor.process(superClass) || !processSupers(superClass, superProcessor, visited)) return false;
    }
    return true;
  }

  public static boolean isInheritor(@Nullable PsiType type, @NotNull @NonNls final String baseClassName) {
    if (type instanceof PsiClassType) {
      return isInheritor(((PsiClassType)type).resolve(), baseClassName);
    }

    return false;
  }

  public static boolean isInheritor(@Nullable PsiClass psiClass, @NotNull final String baseClassName) {
    return isInheritor(psiClass, false, baseClassName);
  }

  public static boolean isInheritor(@Nullable PsiClass psiClass, final boolean strict, @NotNull final String baseClassName) {
    if (psiClass == null) {
      return false;
    }

    final PsiClass base = JavaPsiFacade.getInstance(psiClass.getProject()).findClass(baseClassName, psiClass.getResolveScope());
    if (base == null) {
      return false;
    }

    return strict ? psiClass.isInheritor(base, true) : isInheritorOrSelf(psiClass, base, true);
  }

  /**
   * Gets all superclasses. Classes are added to result in DFS order
   * @param aClass
   * @param results
   * @param includeNonProject
   */
  public static void getSuperClasses(@NotNull PsiClass aClass, @NotNull Set<PsiClass> results, boolean includeNonProject) {
    getSuperClassesOfList(aClass.getSuperTypes(), results, includeNonProject, new THashSet<PsiClass>(), aClass.getManager());
  }

  private static void getSuperClassesOfList(@NotNull PsiClassType[] types,
                                            @NotNull Set<PsiClass> results,
                                            boolean includeNonProject,
                                            @NotNull Set<PsiClass> visited,
                                            @NotNull PsiManager manager) {
    for (PsiClassType type : types) {
      PsiClass resolved = type.resolve();
      if (resolved != null && visited.add(resolved)) {
        if (includeNonProject || manager.isInProject(resolved)) {
          results.add(resolved);
        }
        getSuperClassesOfList(resolved.getSuperTypes(), results, includeNonProject, visited, manager);
      }
    }
  }
}
