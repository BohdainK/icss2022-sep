package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.ElseClause;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.PropertyName;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	// Accumulator attributes:
	private AST ast;

	// Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}
    public AST getAST() {
        return ast;
    }

	// For readability
	private void popContainerAndAddToParent() {
		ASTNode node = currentContainer.pop();
		currentContainer.peek().addChild(node);
	}

	private void pushContainer(ASTNode node) {
		currentContainer.push(node);
	}

	private void addNodeToCurrentContainer(ASTNode node) {
		currentContainer.peek().addChild(node);
	}

	// stylesheet
	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		pushContainer(ast.root);
	}

	@Override
	public void enterStyleRule(ICSSParser.StyleRuleContext ctx) {
		pushContainer(new Stylerule());
	}

	@Override
	public void exitStyleRule(ICSSParser.StyleRuleContext ctx) {
		popContainerAndAddToParent();
	}

	@Override
	public void exitClassSelector(ICSSParser.ClassSelectorContext ctx) {
		ClassSelector classSelector = new ClassSelector(ctx.getText());
		addNodeToCurrentContainer(classSelector);
	}

	@Override
	public void exitIdSelector(ICSSParser.IdSelectorContext ctx) {
		IdSelector idSelector = new IdSelector(ctx.getText());
		addNodeToCurrentContainer(idSelector);
	}

	@Override
	public void exitTagSelector(ICSSParser.TagSelectorContext ctx) {
		TagSelector tagSelector = new TagSelector(ctx.getText());
		addNodeToCurrentContainer(tagSelector);
	}

	// declaration
	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		pushContainer(new Declaration());
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = (Declaration) currentContainer.pop();
		addNodeToCurrentContainer(declaration);
	}

	// property name
	@Override
	public void enterPropertyName(ICSSParser.PropertyNameContext ctx) {
		PropertyName property = new PropertyName(ctx.getText());
		addNodeToCurrentContainer(property);
	}

	@Override
	public void exitPropertyName(ICSSParser.PropertyNameContext ctx) {
		PropertyName property = new PropertyName(ctx.getText());
		addNodeToCurrentContainer(property);
	}

	// variables
	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		pushContainer(new VariableAssignment());
	}

	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = (VariableAssignment) currentContainer.pop();
		addNodeToCurrentContainer(variableAssignment);
	}

	@Override
	public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) {
		VariableReference variableReference = new VariableReference(ctx.getText());
		addNodeToCurrentContainer(variableReference);
	}

	@Override
	public void exitBoolLiteral(ICSSParser.BoolLiteralContext ctx) {
		BoolLiteral boolLiteral = new BoolLiteral(ctx.getText());
		addNodeToCurrentContainer(boolLiteral);
	}

	@Override
	public void exitColorLiteral(ICSSParser.ColorLiteralContext ctx) {
		ColorLiteral colorLiteral = new ColorLiteral(ctx.getText());
		addNodeToCurrentContainer(colorLiteral);
	}

	@Override
	public void exitPixelLiteral(ICSSParser.PixelLiteralContext ctx) {
		PixelLiteral pixelLiteral = new PixelLiteral(ctx.getText());
		addNodeToCurrentContainer(pixelLiteral);
	}

	@Override
	public void exitScalarLiteral(ICSSParser.ScalarLiteralContext ctx) {
		ScalarLiteral scalarLiteral = new ScalarLiteral(ctx.getText());
		addNodeToCurrentContainer(scalarLiteral);
	}


	// expression
	@Override
	public void enterExpression(ICSSParser.ExpressionContext ctx) {
		if (ctx.getChildCount() >= 3 && ctx.getChild(1) != null) {
			String operation = ctx.getChild(1).getText();
			switch (operation) {
				case "+":
					currentContainer.push(new AddOperation());
					break;
				case "-":
					currentContainer.push(new SubtractOperation());
					break;
				case "*":
					currentContainer.push(new MultiplyOperation());
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void exitExpression(ICSSParser.ExpressionContext ctx) {
		if (ctx.getChildCount() >= 3 && ctx.getChild(1) != null) {
			ASTNode operation = currentContainer.pop();
			addNodeToCurrentContainer(operation);
		}
	}

	// if clause
	@Override
	public void enterIfClause(ICSSParser.IfClauseContext ctx) {
		pushContainer(new IfClause());
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause ifClause = (IfClause) currentContainer.pop();
		addNodeToCurrentContainer(ifClause);
	}


	// else clause
	@Override
	public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
		pushContainer(new ElseClause());
	}

	@Override
	public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
		popContainerAndAddToParent();
	}

}