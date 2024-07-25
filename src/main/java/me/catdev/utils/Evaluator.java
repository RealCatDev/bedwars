package me.catdev.utils;

import me.catdev.Bedwars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Evaluator {

    enum AstType {
        AST_ROOT,
        AST_TEXT,
        AST_EXPR,
        AST_VAR,
        AST_BINOP_ADD,
        AST_BINOP_SUB;
    };

    static class Ast {
        public final AstType type;

        public Ast(AstType type) {
            this.type = type;
        }

        public Object value = null;
        public ArrayList<Ast> children = new ArrayList<>();
    }

    private static Ast parse(String str) {
        Ast rootAst = new Ast(AstType.AST_ROOT);

        Stack<Ast> astStack = new Stack<>();
        astStack.push(rootAst);

        for (char c: str.toCharArray()) {
            if (c == '%') {
                if (astStack.lastElement().type == AstType.AST_VAR) {
                    Ast ast = astStack.pop();
                    astStack.lastElement().children.add(ast);
                } else {
                    if (astStack.lastElement().type == AstType.AST_TEXT) {
                        Ast ast = astStack.pop();
                        astStack.lastElement().children.add(ast);
                    }
                    Ast ast = new Ast(AstType.AST_VAR);
                    ast.value = new StringBuilder();
                    astStack.push(ast);
                }
            } else if (c == '{') {
                astStack.push(new Ast(AstType.AST_EXPR));
            } else if (c == '}') {
                if (astStack.lastElement().type == AstType.AST_EXPR
                 || astStack.lastElement().type == AstType.AST_BINOP_ADD
                 || astStack.lastElement().type == AstType.AST_BINOP_SUB) {
                    Ast ast = astStack.pop();
                    astStack.lastElement().children.add(ast);
                }
            } else {
                if (astStack.size() == 1) {
                    Ast newAst = new Ast(AstType.AST_TEXT);
                    newAst.value = new StringBuilder();
                    astStack.push(newAst);
                }
                if (astStack.lastElement().type == AstType.AST_TEXT || astStack.lastElement().type == AstType.AST_VAR) {
                    ((StringBuilder)astStack.lastElement().value).append(c);
                }
                if (astStack.lastElement().type == AstType.AST_EXPR) {
                    AstType type = null;
                    switch (c) {
                        case '+': {
                            type = AstType.AST_BINOP_ADD;
                        } break;
                        case '-': {
                            type = AstType.AST_BINOP_SUB;
                        } break;
                    }
                    if (type != null) {
                        if (astStack.lastElement().children.size() == 1) {
                            Ast lhs = astStack.lastElement().children.get(0);
                            Ast ast = new Ast(type);
                            ast.children.add(lhs);
                            astStack.pop();
                            astStack.push(ast);
                        }
                    }
                }
            }
        }

        if (astStack.size() > 2) return rootAst;
        if (astStack.size() == 2) {
            rootAst.children.add(astStack.pop());
        }

        return rootAst;
    }

    private static Object evaluate(Ast ast, HashMap<String, Object> variables) {
        if (ast == null) return "";
        switch (ast.type) {
            case AST_ROOT: {
                if (ast.children.isEmpty()) return "";
                StringBuilder evaluated = new StringBuilder();
                for (Ast child : ast.children) {
                    Object ev = evaluate(child, variables);
                    if (ev != null) {
                        evaluated.append(ev);
                    }
                }
                return evaluated.toString();
            }
            case AST_TEXT: {
                return ast.value;
            }
            case AST_VAR: {
                if (variables.isEmpty()) return "";
                return (variables.containsKey(ast.value.toString())?variables.get(ast.value.toString()):ast.value.toString());
            }
            case AST_EXPR: {
                if (ast.children.isEmpty()) return "";
                return evaluate(ast.children.get(0), variables);
            }
            case AST_BINOP_ADD: {
                if (ast.children.size() != 2) return "";
                Object lhs = evaluate(ast.children.get(0), variables);
                Object rhs = evaluate(ast.children.get(1), variables);
                if (lhs instanceof Integer) return (Integer)lhs + (Integer)rhs;
                return "";
            }
            case AST_BINOP_SUB: {
                if (ast.children.size() != 2) return "";
                Object lhs = evaluate(ast.children.get(0), variables);
                Object rhs = evaluate(ast.children.get(1), variables);
                if (lhs instanceof Integer) return (Integer)lhs - (Integer)rhs;
                return "";
            }
            default: {
                return "";
            }
        }
    }

    public static String evaluate(String str, HashMap<String, Object> variables) {
        Ast ast = parse(str);
        if (ast.type != AstType.AST_ROOT) return "";
        if (ast.children.isEmpty()) return "";
        return (String)evaluate(ast, variables);
    }

}
