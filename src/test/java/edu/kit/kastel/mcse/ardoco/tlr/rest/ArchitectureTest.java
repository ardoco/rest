/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "edu.kit.kastel.mcse.ardoco.tlr.rest")
class ArchitectureTest {

    @ArchTest
    static ArchRule disallowLog4j = noClasses().that()
            .haveNameNotMatching(ArchitectureTest.class.getName())
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName(LogManager.class.getName())
            .orShould()
            .dependOnClassesThat()
            .haveFullyQualifiedName(Logger.class.getName());
}
