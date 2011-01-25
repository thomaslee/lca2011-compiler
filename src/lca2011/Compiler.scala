package lca2011

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.FileInputStream

import org.apache.bcel.Constants
import org.apache.bcel.generic.ClassGen
import org.apache.bcel.generic.MethodGen
import org.apache.bcel.generic.InstructionList
import org.apache.bcel.generic.InstructionFactory
import org.apache.bcel.generic.ConstantPoolGen
import org.apache.bcel.generic.ArrayType
import org.apache.bcel.generic.ObjectType
import org.apache.bcel.generic.Type

import scala.util.parsing.combinator.RegexParsers

trait AST {}
trait Expr extends AST {}
// trait Stmt extends AST {}

case class Call(name : Name, args : List[Expr]) extends Expr
case class Name(id : String) extends Expr
case class Str(value : String) extends Expr

object Compiler extends RegexParsers {
    val STR = "\"[^\"]*\"" r
    val ID = "[a-zA-Z_][a-zA-Z_0-9]*" r

    def stmt = (expr <~ ";")
    
    def expr : Parser[Expr] = call | str

    def call = name ~ ("(" ~> args <~ ")") ^^ { case name ~ args =>
        new Call(name, args)
    }

    def args = repsep(expr, ",")

    def str = STR ^^ { case value => new Str(value.substring(1, value.length()-1)) }

    def name = ID ^^ { case id => new Name(id) }

    def main(args : Array[String]) = {
        val in = new FileInputStream(args(0))
        val reader = new BufferedReader(new InputStreamReader(in))
        parseAll(stmt, reader) match {
            case Success(res, _) => { generateClass(args(0), res) }
            case e => {
                println(e)
                System.exit(1)
            }
        }
    }

    def generateClass(filename : String, expr : Expr) {
        val cgen = new ClassGen("Program", "java.lang.Object", filename, Constants.ACC_PUBLIC, Array())
        val pgen = cgen.getConstantPool()
        val ifact = new InstructionFactory(cgen, pgen)
        val ilist = new InstructionList()

        prelude(cgen, ifact)

        val mainMethod = new MethodGen(
            Constants.ACC_PUBLIC | Constants.ACC_STATIC, // access flags
            Type.VOID,                                   // return type
            Array(new ArrayType(Type.STRING, 1)),        // arg types
            Array("args"),                               // arg names
            "main",                                      // method name
            cgen.getClassName(),                         // class name
            ilist,
            pgen)

        visitExpr(cgen, pgen, ilist, expr)

        ilist.append(InstructionFactory.createReturn(Type.VOID))

        mainMethod.setMaxStack()
        cgen.addMethod(mainMethod.getMethod())
        cgen.getJavaClass().dump(System.out)
    }

    def visitExpr(cgen : ClassGen, pgen : ConstantPoolGen, ilist : InstructionList, expr : Expr) {
        val ifact = new InstructionFactory(cgen, pgen)
        expr match {
            case Call(name, args) => {
                for (arg <- args) {
                    visitExpr(cgen, pgen, ilist, arg)
                }
                ilist.append(ifact.createInvoke("Program", "puts", Type.VOID, Array(Type.STRING), Constants.INVOKESTATIC))
            }
            case Str(value) => {
                ilist.append(ifact.createConstant(value))
            }
            case e => {
                println("unknown expression type: " + e)
                System.exit(1)
            }
        }
    }

    def visitExprList(cgen : ClassGen, pgen : ConstantPoolGen, ilist : InstructionList, exprs : List[Expr]) {
        for (expr <- exprs) {
            visitExpr(cgen, pgen, ilist, expr)
        }
    }
    
    def prelude(cgen : ClassGen, ifact : InstructionFactory) = {
        cgen.addEmptyConstructor(Constants.ACC_PUBLIC)

        builtinPuts(cgen, ifact)
    }

    def builtinPuts(cgen : ClassGen, ifact : InstructionFactory) = {
        val ilist = new InstructionList()
        val putsMethod = new MethodGen(
            Constants.ACC_PRIVATE | Constants.ACC_STATIC, // access flags
            Type.VOID,                                    // return type
            Array(Type.STRING),                           // arg types
            Array("s"),                                   // arg names
            "puts",                                       // method name
            cgen.getClassName(),                          // class name
            ilist,
            cgen.getConstantPool())
        putsMethod.setInstructionList(ilist)

        ilist.append(ifact.createGetStatic("java.lang.System", "out", new ObjectType("java.io.PrintStream")))
        ilist.append(InstructionFactory.createLoad(Type.STRING, 0))
        ilist.append(ifact.createInvoke("java.io.PrintStream", "println", Type.VOID, Array(Type.STRING), Constants.INVOKEVIRTUAL))
        ilist.append(InstructionFactory.createReturn(Type.VOID))

        putsMethod.setMaxStack()
        cgen.addMethod(putsMethod.getMethod())
    }
}

// vim: set ts=4 sw=4 et:
