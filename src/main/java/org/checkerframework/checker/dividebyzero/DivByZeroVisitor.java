package org.checkerframework.checker.dividebyzero;

import com.sun.source.tree.*;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.type.TypeKind;
import org.checkerframework.checker.dividebyzero.qual.*;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

public class DivByZeroVisitor extends BaseTypeVisitor<DivByZeroAnnotatedTypeFactory> {

  /** Set of operators we care about */
  private static final Set<Tree.Kind> DIVISION_OPERATORS =
      EnumSet.of(
          /* x /  y */ Tree.Kind.DIVIDE,
          /* x /= y */ Tree.Kind.DIVIDE_ASSIGNMENT,
          /* x %  y */ Tree.Kind.REMAINDER,
          /* x %= y */ Tree.Kind.REMAINDER_ASSIGNMENT);

  /**
   * Determine whether to report an error at the given binary AST node. The error text is defined in
   * the messages.properties file.
   *
   * @param node the AST node to inspect
   * @return true if an error should be reported, false otherwise
   */
  private boolean errorAt(BinaryTree node) {
    // A BinaryTree can represent any binary operator, including + or -.
    if (node == null) {
        return false;
    }
    Tree.Kind nodeKind = node.getKind();
    // Check if node's kind is in DIVISION_OPERATORS
    if (DIVISION_OPERATORS.contains(nodeKind)) {
      // Get right operand from tree: 
      // https://docs.oracle.com/en/java/javase/17/docs/api/jdk.compiler/com/sun/source/tree/BinaryTree.html 
      Tree rightOperand = node.getRightOperand();
      if (rightOperand != null && (hasAnnotation(rightOperand, Top.class) || hasAnnotation(rightOperand, Zero.class))) {
            return true; 
      }
    }
      return false; 
  }

  /**
   * Determine whether to report an error at the given compound assignment AST node. The error text
   * is defined in the messages.properties file.
   *
   * @param node the AST node to inspect
   * @return true if an error should be reported, false otherwise
   */
  private boolean errorAt(CompoundAssignmentTree node) {
    // A CompoundAssignmentTree represents any binary operator combined with an assignment,
    // such as "x += 10".
    if (node == null) {
        return false;
    }
    Tree.Kind nodeKind = node.getKind();
    // Check if node's kind is in DIVISION_OPERATORS
    if (DIVISION_OPERATORS.contains(nodeKind)) {
      // Get right hand side expression of the compound assignment from tree: 
      // https://docs.oracle.com/en/java/javase/17/docs/api/jdk.compiler/com/sun/source/tree/CompoundAssignmentTree.html
      Tree expression = node.getExpression();
      if (expression != null && (hasAnnotation(expression, Top.class) || hasAnnotation(expression, Zero.class))) {
            return true; 
      }
    }
      return false; 
  }

  // ========================================================================
  // Useful helpers

  private static final Set<TypeKind> INT_TYPES = EnumSet.of(TypeKind.INT, TypeKind.LONG);

  private boolean isInt(Tree node) {
    return INT_TYPES.contains(atypeFactory.getAnnotatedType(node).getKind());
  }

  private boolean hasAnnotation(Tree node, Class<? extends Annotation> c) {
    return atypeFactory.getAnnotatedType(node).hasPrimaryAnnotation(c);
  }

  // ========================================================================
  // Checker Framework plumbing

  public DivByZeroVisitor(BaseTypeChecker c) {
    super(c);
  }

  @Override
  public Void visitBinary(BinaryTree node, Void p) {
    if (isInt(node)) {
      if (errorAt(node)) {
        checker.reportError(node, "divide.by.zero");
      }
    }
    return super.visitBinary(node, p);
  }

  @Override
  public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
    if (isInt(node.getExpression())) {
      if (errorAt(node)) {
        checker.reportError(node, "divide.by.zero");
      }
    }
    return super.visitCompoundAssignment(node, p);
  }
}
