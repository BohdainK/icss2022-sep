package nl.han.ica.icss.checker;

import java.util.HashMap;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.ElseClause;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.Stylesheet;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        variableTypes.addFirst(new HashMap<>());
        checkNode(ast.root);
    }

    private void checkNode(ASTNode node) {
        // Stylesheet
        if (node instanceof Stylesheet) {
            for (ASTNode child : node.getChildren()) {
                checkNode(child);
            }
            return;
        }

        // Stylerule
        if (node instanceof Stylerule) {
            variableTypes.addFirst(new HashMap<>());
            for (ASTNode child : node.getChildren()) {
                checkNode(child);
            }
            variableTypes.removeFirst();
            return;
        }

        // IfClause
        if (node instanceof IfClause) {
            IfClause ifClause = (IfClause) node;
            ExpressionType conditionType = evaluateExpression(ifClause.getConditionalExpression());
            if (conditionType != ExpressionType.BOOL) {
                node.setError("expression not bool");
            }
            variableTypes.addFirst(new HashMap<>());
            for (ASTNode child : ifClause.body) {
                checkNode(child);
            }
            if (ifClause.getElseClause() != null) {
                checkNode(ifClause.getElseClause());
            }
            variableTypes.removeFirst();
            return;
        }

        // ElseClause
        if (node instanceof ElseClause) {
            variableTypes.addFirst(new HashMap<>());
            for (ASTNode child : node.getChildren()) {
                checkNode(child);
            }
            variableTypes.removeFirst();
            return;
        }

        // Variable assignment
        if (node instanceof VariableAssignment) {
            VariableAssignment variableAssignment = (VariableAssignment) node;
            ExpressionType type = evaluateExpression(variableAssignment.expression);
            String name = variableAssignment.name.name;
            if (type == ExpressionType.UNDEFINED) {
                variableAssignment.setError("assignment has undefined expression");
            } else {
                variableTypes.getFirst().put(name, type);
            }
            return;
        }

        // Property declaration
        if (node instanceof Declaration) {
            Declaration decleration = (Declaration) node;
            ExpressionType type = evaluateExpression(decleration.expression);
            if (type == ExpressionType.UNDEFINED) {
                decleration.setError("declaration has undefined expression");
                return;
            }
            String property = decleration.property.name.toLowerCase();
            if (property.contains("color")) {
                if (type != ExpressionType.COLOR) {
                    node.setError("not color literal " + property);
                }
            } else {
                if (type == ExpressionType.COLOR) {
                    node.setError("Color cannot be used for property " + property);
                }
            }
            return;
        }

        for (ASTNode child : node.getChildren()) {
            checkNode(child);
        }
    }

    // Evaluate the type of an expression
    private ExpressionType evaluateExpression(Expression expression) {
        if (expression == null) {
            return ExpressionType.UNDEFINED;
        }

        // Literals
        if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        }
        if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        }
        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        }
        if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        }
        if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        }

        // Variable reference
        if (expression instanceof VariableReference) {
            String name = ((VariableReference) expression).name;
            for (int i = 0; i < variableTypes.getSize(); i++) {
                HashMap<String, ExpressionType> scope = variableTypes.get(i);
                if (scope.containsKey(name)) {
                    return scope.get(name);
                }
            }
            expression.setError("'" + name + "' not defined");
            return ExpressionType.UNDEFINED;
        }

        // Operations
        if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            ExpressionType lhs = evaluateExpression(operation.lhs);
            ExpressionType rhs = evaluateExpression(operation.rhs);
            if (lhs == ExpressionType.COLOR || rhs == ExpressionType.COLOR) {
                expression.setError("Colors cannot be used in operations");
                return ExpressionType.UNDEFINED;
            }

            // Addition and Subtraction
            if (expression instanceof AddOperation || expression instanceof SubtractOperation) {
                if (lhs != rhs) {
                    expression.setError("must be same type for + and -");
                    return ExpressionType.UNDEFINED;
                }
                return lhs;
            }

            // Multiplication
            if (expression instanceof MultiplyOperation) {
                if (lhs == ExpressionType.SCALAR && rhs != ExpressionType.UNDEFINED) {
                    return rhs;
                }
                if (rhs == ExpressionType.SCALAR && lhs != ExpressionType.UNDEFINED) {
                    return lhs;
                }
                expression.setError("Multiplication requires one or more scalar values");
                return ExpressionType.UNDEFINED;
            }
        }

        return ExpressionType.UNDEFINED;
    }

}
