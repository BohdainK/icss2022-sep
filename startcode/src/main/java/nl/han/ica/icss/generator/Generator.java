package nl.han.ica.icss.generator;

import java.util.stream.Collectors;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.Stylesheet;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;

public class Generator {

    public String generate(AST ast) {
        return generateStylesheet(ast.root);
    }

    //CSS from stylesheet
    private String generateStylesheet(Stylesheet stylesheet) {
        StringBuilder css = new StringBuilder();

        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof Stylerule) {
                css.append(generateStylerule((Stylerule) child)).append("\n");
            }
        }

        return css.toString();
    }

    //CSS for single style rule
    private String generateStylerule(Stylerule stylerule) {
        StringBuilder css = new StringBuilder();

        css.append(generateSelectors(stylerule));
        css.append(generateDeclarations(stylerule));
        css.append("}\n");

        return css.toString();
    }

    //CSS selectors for rule
    private String generateSelectors(Stylerule stylerule) {
        return stylerule.selectors.stream()
                .map(ASTNode::toString)
                .collect(Collectors.joining(", ")) + " {\n";
    }

    //CSS declarations inside rule
    private String generateDeclarations(Stylerule stylerule) {
        StringBuilder css = new StringBuilder();

        for (ASTNode child : stylerule.getChildren()) {
            if (child instanceof Declaration) {
                css.append("  ");
                css.append(generateDeclaration((Declaration) child));
            }
        }
        return css.toString();
    }

    //CSS for single property and value
    private String generateDeclaration(Declaration declaration) {
        return declaration.property.name + ": "
                + generateExpression(declaration.expression)
                + ";\n";
    }

    //CSS for expression values
    private String generateExpression(Expression expression) {
        if (expression instanceof PercentageLiteral) {
            return ((PercentageLiteral) expression).value + "%";
        } else if (expression instanceof PixelLiteral) {
            return ((PixelLiteral) expression).value + "px";
        } else if (expression instanceof ColorLiteral) {
            return ((ColorLiteral) expression).value;
        } else if (expression instanceof ScalarLiteral) {
            return Integer.toString((int) ((ScalarLiteral) expression).value);
        }
        return "";
    }
}
