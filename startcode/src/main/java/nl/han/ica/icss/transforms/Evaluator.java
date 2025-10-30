package nl.han.ica.icss.transforms;

import java.util.HashMap;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.Literal;
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

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
        variableValues.addFirst(new HashMap<>());
    }

    @Override
    public void apply(AST ast) {
        evaluateStylesheet(ast.root);
    }

    private void evaluateStylesheet(Stylesheet stylesheet) {
        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) child);
            } else if (child instanceof Stylerule) {
                evaluateStylerule((Stylerule) child);
            }
        }
    }

    private void evaluateVariableAssignment(VariableAssignment variableAssignment) {
        Literal value = evaluateExpression(variableAssignment.expression);

        if (value != null) {
            variableValues.getFirst().put(variableAssignment.name.name, value);
        } else {
            throw new RuntimeException("error on: " + variableAssignment.name);
        }
    }

    private void evaluateStylerule(Stylerule stylerule) {
        IHANLinkedList<ASTNode> evaluatedChildren = new HANLinkedList<>();

        // Insert nodes in order using insert
        int index = 0;
        for (int i = 0; i < stylerule.getChildren().size(); i++) {
            ASTNode child = stylerule.getChildren().get(i);

            if (child instanceof Declaration) {
                evaluateDeclaration((Declaration) child);
                evaluatedChildren.insert(index++, child);
            } else if (child instanceof IfClause) {
                index = evaluateIfClause((IfClause) child, evaluatedChildren, index);
            } else {
                evaluatedChildren.insert(index++, child);
            }
        }

        stylerule.getChildren().clear();
        for (int i = 0; i < evaluatedChildren.getSize(); i++) {
            stylerule.getChildren().add(i, evaluatedChildren.get(i));
        }
    }

    private int evaluateIfClause(IfClause ifClause, IHANLinkedList<ASTNode> evaluatedChildren, int startIndex) {
        Literal condition = evaluateExpression(ifClause.conditionalExpression);

        if (!(condition instanceof BoolLiteral)) {
            throw new RuntimeException("not bool");
        }

        BoolLiteral bool = (BoolLiteral) condition;
        int index = startIndex;

        if (bool.value) {
            for (int i = 0; i < ifClause.body.size(); i++) {
                ASTNode node = ifClause.body.get(i);
                if (node instanceof Declaration) {
                    evaluateDeclaration((Declaration) node);
                }
                evaluatedChildren.insert(index++, node);
            }
        } else if (ifClause.elseClause != null) {
            // FALSE → insert else body if exists
            for (int i = 0; i < ifClause.elseClause.body.size(); i++) {
                ASTNode node = ifClause.elseClause.body.get(i);
                if (node instanceof Declaration) {
                    evaluateDeclaration((Declaration) node);
                }
                evaluatedChildren.insert(index++, node);
            }
        }
        return index;
    }

    // private void evaluateStylerule(Stylerule stylerule) {
    //     IHANLinkedList<ASTNode> evaluatedChildren = new HANLinkedList<>();
    //     for (ASTNode child : stylerule.getChildren()) {
    //         if (child instanceof Declaration) {
    //             evaluateDeclaration((Declaration) child);
    //             evaluatedChildren.add(child);
    //         } else if (child instanceof IfClause) {
    //             evaluateIfClause((IfClause) child, evaluatedChildren);
    //         } else {
    //             evaluatedChildren.add(child);
    //         }
    //     }
    //     stylerule.setChildren(evaluatedChildren);
    // }
    // private void evaluateIfClause(IfClause ifClause, IHANLinkedList<ASTNode> evaluatedChildren) {
    //     Literal condition = evaluateExpression(ifClause.conditionalExpression);
    //     if (condition instanceof BoolLiteral) {
    //         BoolLiteral bool = (BoolLiteral) condition;
    //         if (bool.value) {
    //             // If TRUE → add all nodes in if body
    //             for (ASTNode node : ifClause.body) {
    //                 if (node instanceof Declaration) {
    //                     evaluateDeclaration((Declaration) node);
    //                 }
    //                 evaluatedChildren.add(node);
    //             }
    //         } else {
    //             // If FALSE → add else body if it exists
    //             if (ifClause.elseClause != null) {
    //                 for (ASTNode node : ifClause.elseClause.body) {
    //                     if (node instanceof Declaration) {
    //                         evaluateDeclaration((Declaration) node);
    //                     }
    //                     evaluatedChildren.add(node);
    //                 }
    //             }
    //             // If FALSE and no elseClause → do nothing (IfClause is removed)
    //         }
    //     } else {
    //         throw new RuntimeException("Condition in IfClause did not evaluate to a BoolLiteral.");
    //     }
    // }
    private void evaluateDeclaration(Declaration declaration) {
        Literal value = evaluateExpression(declaration.expression);

        if (value != null) {
            declaration.expression = value;
        } else {
            throw new RuntimeException("Error on: " + declaration.property.name);
        }
    }

    private Literal evaluateExpression(Expression expression) {
        if (expression instanceof VariableReference) {
            return variableValues.getFirst().get(((VariableReference) expression).name);
        } else if (expression instanceof AddOperation || expression instanceof SubtractOperation || expression instanceof MultiplyOperation) {
            return evaluateBinaryOperation(expression);
        } else if (expression instanceof PixelLiteral) {
            return (PixelLiteral) expression;
        } else if (expression instanceof PercentageLiteral) {
            return (PercentageLiteral) expression;
        } else if (expression instanceof ScalarLiteral) {
            return (ScalarLiteral) expression;
        } else if (expression instanceof ColorLiteral) {
            return (ColorLiteral) expression;
        } else if (expression instanceof BoolLiteral) {
            return (BoolLiteral) expression;
        }
        return null;
    }

    private Literal evaluateBinaryOperation(Expression expression) {
        if (expression instanceof AddOperation) {
            AddOperation addOp = (AddOperation) expression;
            Literal lhs = evaluateExpression(addOp.lhs);
            Literal rhs = evaluateExpression(addOp.rhs);
            if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
                double result = ((ScalarLiteral) lhs).value + ((ScalarLiteral) rhs).value;
                return new ScalarLiteral(Double.toString(result));
            } else if (lhs instanceof PixelLiteral && rhs instanceof PixelLiteral) {
                int result = ((PixelLiteral) lhs).value + ((PixelLiteral) rhs).value;
                return new PixelLiteral(Integer.toString(result) + "px");
            } else if (lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral) {
                double result = ((PercentageLiteral) lhs).value + ((PercentageLiteral) rhs).value;
                return new PercentageLiteral(Double.toString(result) + "%");
            } else {
                return null;
            }

        } else if (expression instanceof SubtractOperation) {
            SubtractOperation subOp = (SubtractOperation) expression;
            Literal lhs = evaluateExpression(subOp.lhs);
            Literal rhs = evaluateExpression(subOp.rhs);
            if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
                double result = ((ScalarLiteral) lhs).value - ((ScalarLiteral) rhs).value;
                return new ScalarLiteral(Double.toString(result));
            } else if (lhs instanceof PixelLiteral && rhs instanceof PixelLiteral) {
                int result = ((PixelLiteral) lhs).value - ((PixelLiteral) rhs).value;
                return new PixelLiteral(Integer.toString(result) + "px");
            } else if (lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral) {
                double result = ((PercentageLiteral) lhs).value - ((PercentageLiteral) rhs).value;
                return new PercentageLiteral(Double.toString(result) + "%");
            } else {
                return null;
            }

        } else if (expression instanceof MultiplyOperation) {
            MultiplyOperation mulOp = (MultiplyOperation) expression;
            Literal lhs = evaluateExpression(mulOp.lhs);
            Literal rhs = evaluateExpression(mulOp.rhs);
            if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
                double result = ((ScalarLiteral) lhs).value * ((ScalarLiteral) rhs).value;
                return new ScalarLiteral(Double.toString(result));

            } else if (lhs instanceof ScalarLiteral) {
                if (rhs instanceof PixelLiteral) {
                    int result = (int) (((ScalarLiteral) lhs).value * ((PixelLiteral) rhs).value);
                    return new PixelLiteral(Integer.toString(result) + "px");
                } else if (rhs instanceof PercentageLiteral) {
                    double result = ((ScalarLiteral) lhs).value * ((PercentageLiteral) rhs).value;
                    return new PercentageLiteral(Double.toString(result) + "%");
                }

            } else if (rhs instanceof ScalarLiteral) {
                if (lhs instanceof PixelLiteral) {
                    int result = (int) (((ScalarLiteral) rhs).value * ((PixelLiteral) lhs).value);
                    return new PixelLiteral(Integer.toString(result) + "px");
                } else if (lhs instanceof PercentageLiteral) {
                    double result = ((ScalarLiteral) rhs).value * ((PercentageLiteral) lhs).value;
                    return new PercentageLiteral(Double.toString(result) + "%");
                }
            }
        }
        return null;
    }
}
