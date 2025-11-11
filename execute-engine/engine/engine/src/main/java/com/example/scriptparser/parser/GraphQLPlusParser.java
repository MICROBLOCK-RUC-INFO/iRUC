package com.example.scriptparser.parser;

import com.example.scriptparser.ast.*;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.*;
import org.parboiled.support.StringVar;
import org.parboiled.support.Var;

import java.util.ArrayList;
import java.util.List;

@BuildParseTree
public class GraphQLPlusParser extends BaseParser<Object> {

    public Rule Script() {
        return Sequence(
                push(new ArrayList<Statement>()),
                Spacing(),
                ZeroOrMore(
                        Statement(),
                        ACTION(addToList())
                ),
                Spacing(),
                EOI
        );
    }

    boolean addToList() {
        Object top = pop();
        if (top instanceof Statement stmt) {
            @SuppressWarnings("unchecked")
            List<Statement> statements = (List<Statement>) peek();
            statements.add(stmt);
            return true;
        }
        return false;
    }

    Rule Statement() {
        return Sequence(
                FirstOf(
                        GqlQueryStatement(),
                        DataAlignStatement(),
                        PluginCallStatement(),
                        DefStatement(),
                        SetStatement(),
                        IntDefStatement(),
                        IntCalStatement(),
                        IfStatement(),
                        WhileStatement()
                ),
                OptionalSemicolon()
        );
    }

    Rule OptionalSemicolon() {
        return Sequence(
                Spacing(),
                Optional(Ch(';')),
                Spacing()
        );
    }

    Rule GqlQueryStatement() {
        StringVar varName = new StringVar();
        StringVar queryBody = new StringVar();

        return Sequence(
                String("new"), Spacing(),
                RawIdentifier(), varName.set(match()), Spacing(),
                Ch('='), Spacing(),
                String("gql"), Spacing(), String("query"), Spacing(),
                GraphQLQueryBody(), queryBody.set(match()),
                push(new GqlQueryStatement(varName.get(), queryBody.get()))
        );
    }

    Rule DataAlignStatement() {
        StringVar outputVar = new StringVar();
        StringVar sourceVar = new StringVar();

        return Sequence(
                String("output"), Spacing(),
                RawIdentifierWithDots(), outputVar.set(match()), Spacing(),
                Ch('='), Spacing(),
                RawIdentifier(), sourceVar.set(match()),
                push(new DataAlignStatement(outputVar.get(), sourceVar.get()))
        );
    }

    Rule PluginCallStatement() {
        StringVar varName = new StringVar();
        StringVar pluginName = new StringVar();
        StringVar funcName = new StringVar();
        Var<List<String>> args = new Var<>(new ArrayList<>());

        return Sequence(
                String("new"), Spacing(),
                RawIdentifier(), varName.set(match()), Spacing(),
                Ch('='), Spacing(),
                RawIdentifierWithDots(), pluginName.set(match()), Spacing(),
                Ch('/'), Spacing(),
                RawIdentifier(), funcName.set(match()), Spacing(),
                Ch('('), Spacing(),
                Optional(ArgumentList(args)), Spacing(),
                Ch(')'),
                push(new PluginCallStatement(varName.get(), pluginName.get(), funcName.get(), args.get()))
        );
    }

    Rule ArgumentList(Var<List<String>> args) {
        StringVar arg = new StringVar();
        return Sequence(
                RawIdentifierWithDots(), arg.set(match()), args.get().add(arg.get()),
                ZeroOrMore(
                        Spacing(), Ch(','), Spacing(),
                        RawIdentifierWithDots(), arg.set(match()), args.get().add(arg.get())
                )
        );
    }

    Rule DefStatement() {
        StringVar varName = new StringVar();

        return Sequence(
                String("def"), Spacing(),
                RawIdentifier(), varName.set(match()), Spacing(),
                Ch('='), Spacing(),
                Value(),
                push(new DefStatement(varName.get(), (ValueNode) pop()))
        );
    }

    Rule SetStatement() {
        StringVar varName = new StringVar();

        return Sequence(
                String("set"), Spacing(),
                RawIdentifier(), varName.set(match()), Spacing(),
                Ch('='), Spacing(),
                Value(),
                push(new SetStatement(varName.get(), (ValueNode) pop()))
        );
    }

    Rule IntDefStatement() {
        StringVar varName = new StringVar();
        StringVar valueStr = new StringVar();

        return Sequence(
                String("int"), Spacing(),
                RawIdentifier(), varName.set(match()), Spacing(),
                Ch('='), Spacing(),
                RawIntLiteral(), valueStr.set(match()),
                push(new IntDefStatement(varName.get(), Integer.parseInt(valueStr.get())))
        );
    }

    Rule IntCalStatement() {
        StringVar targetVar = new StringVar();
        StringVar sourceVar = new StringVar();
        StringVar op = new StringVar();
        StringVar operandStr = new StringVar();

        return Sequence(
                String("cal"), Spacing(),
                RawIdentifier(), targetVar.set(match()), Spacing(),
                Ch('='), Spacing(),
                RawIdentifier(), sourceVar.set(match()), Spacing(),
                RawOperator(), op.set(match()), Spacing(),
                RawIntLiteral(), operandStr.set(match()),
                push(new IntCalStatement(targetVar.get(), sourceVar.get(), op.get(), Integer.parseInt(operandStr.get())))
        );
    }

    Rule IfStatement() {
        StringVar condVar = new StringVar();

        return Sequence(
                String("if"), Spacing(),
                Ch('('), Spacing(), RawIdentifier(), condVar.set(match()), Spacing(), Ch(')'), Spacing(),
                Block(),
                push(new IfStatement(condVar.get(), (Block) pop()))
        );
    }

    Rule WhileStatement() {
        StringVar condVar = new StringVar();

        return Sequence(
                String("while"), Spacing(),
                Ch('('), Spacing(), RawIdentifier(), condVar.set(match()), Spacing(), Ch(')'), Spacing(),
                Block(),
                push(new WhileStatement(condVar.get(), (Block) pop()))
        );
    }

    Rule Block() {
        return Sequence(
                Ch('{'), Spacing(),
                push(new ArrayList<Statement>()),
                ZeroOrMore(
                        Statement(),
                        ACTION(addToList())
                ),
                Ch('}'), Spacing(),
                push(new Block((List<Statement>) pop()))
        );
    }

    Rule Value() {
        return FirstOf(
                BooleanValue(),
                FloatValue(),
                IntValue()
        );
    }

    Rule BooleanValue() {
        return Sequence(
                FirstOf(
                        String("true"),
                        String("false")
                ),
                push(new ValueNode(Boolean.parseBoolean(match()))),
                Spacing()
        );
    }

    Rule FloatValue() {
        return Sequence(
                Sequence(
                        Optional(Ch('-')),
                        FirstOf(
                                Sequence(OneOrMore(Digit()), Ch('.'), ZeroOrMore(Digit())),
                                Sequence(Ch('.'), OneOrMore(Digit()))
                        )
                ),
                push(new ValueNode(Double.parseDouble(match()))),
                Spacing()
        );
    }

    Rule IntValue() {
        return Sequence(
                RawIntLiteral(),
                push(new ValueNode(Integer.parseInt(match()))),
                Spacing()
        );
    }

    Rule RawIdentifier() {
        return Sequence(
                TestNot(ReservedKeyword()),
                Letter(),
                ZeroOrMore(LetterOrDigit())
        );
    }

    Rule RawIdentifierWithDots() {
        return Sequence(
                TestNot(ReservedKeyword()),
                Letter(),
                ZeroOrMore(FirstOf(LetterOrDigit(), Ch('.')))
        );
    }

    Rule RawIntLiteral() {
        return Sequence(
                Optional(Ch('-')),
                OneOrMore(Digit())
        );
    }

    Rule RawOperator() {
        return FirstOf(Ch('+'), Ch('-'));
    }

    Rule GraphQLQueryBody() {
        return Sequence(
                Ch('{'),
                ZeroOrMore(
                        FirstOf(
                                Sequence(Ch('{'), GraphQLBodyContent(), Ch('}')),
                                Sequence(TestNot(AnyOf("{}")), ANY)
                        )
                ),
                Ch('}'),
                Spacing()
        );
    }

    Rule GraphQLBodyContent() {
        return ZeroOrMore(
                FirstOf(
                        Sequence(Ch('{'), GraphQLBodyContent(), Ch('}')),
                        Sequence(TestNot(AnyOf("{}")), ANY)
                )
        );
    }

    Rule ReservedKeyword() {
        return Sequence(
                FirstOf(
                        String("new"),
                        String("gql"),
                        String("query"),
                        String("output"),
                        String("def"),
                        String("set"),
                        String("int"),
                        String("cal"),
                        String("if"),
                        String("while"),
                        String("true"),
                        String("false")
                ),
                TestNot(LetterOrDigit())
        );
    }

    @SuppressNode Rule Letter() { return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), Ch('_')); }
    @SuppressNode Rule LetterOrDigit() { return FirstOf(Letter(), Digit()); }
    @SuppressNode Rule Digit() { return CharRange('0', '9'); }

    @SuppressNode
    Rule Spacing() {
        return ZeroOrMore(FirstOf(
                Sequence(Ch('#'), ZeroOrMore(TestNot(AnyOf("\r\n")), ANY), FirstOf(Ch('\r'), Ch('\n'), EOI)),
                AnyOf(" \t\f\r\n")
        ));
    }
}
