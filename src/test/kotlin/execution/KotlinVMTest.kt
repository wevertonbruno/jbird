package execution

import io.mockk.mockk
import syntactic.tokenizer.Tokenizer
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import syntactic.parser.Parser
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KotlinVMTest {
    private fun getTestSource() = Stream.of(
        Arguments.of("""(1 + 3) * 5;""", 20.0),
        Arguments.of("""2^2^3;""", 256.0),
        Arguments.of("""1 - 2 + 3;""", -2.0),
        Arguments.of("""false ? 5 : 3 < 4 ? 2 : 3 ;""", 2),
        Arguments.of("""42 > 42 ? 42 : 43 > 42 ? 0 : 1;""", 0),
        Arguments.of(""" "one" + ("_" + 1); """, "one_1"),
        Arguments.of(""" print 1 + 2 + 3 + 4 - 5 * 3 > 0; """, "one_1"),
        Arguments.of(""" print "Hello, World!"${"\n"} """, "one_1"),
        Arguments.of("""
            var a = 4
            var b = 2
            print a * b;
        """.trimIndent(), "one_1"),
        Arguments.of("""
            var a = 4
            var b = 2
            print a * b;
            a = 3
            print a + b;
        """.trimIndent(), "one_1"),
        Arguments.of("""
            {
                var a = 3; var b = 2
                print a * b;
                {
                    print a * b;
                    {
                        a = 5
                        print a * b;
                    }
                }
            }
        """.trimIndent(), "one_1"),
        Arguments.of("""
            var a;
            var b = nil;
            a = "assigned";
            print a; // OK, was assigned first.
            print b; // Nil
        """.trimIndent(), "one_1"),
        Arguments.of("""
            var a = 1;
            {
                 var a = a + 2;
                 print a;
            }
        """.trimIndent(), "one_1"),
        Arguments.of("""
            var a = 1;
            if (a < 10) {
                var b = 10
                a = a + b
                if (a < 10)
                    print a
                else
                    print b
            }
            """.trimIndent(), ""),
        Arguments.of("""
            var a = 5;
            while(a >= 0) {
                print a
                a = a - 1
            }
            """.trimIndent(), ""),
        Arguments.of("""
            print "======= Do While ======"
            var a = 5;
            do {
                print a
                a = a - 1
            }while(a > 0);
            """.trimIndent(), ""),
        Arguments.of("""
            print "======= Def functions ======"
            func sayHi(first, last) {
                print "Hi, " + first + " " + last + "!"
            }
            sayHi("Dear", "Reader");
            """.trimIndent(), ""),
        Arguments.of("""
            print "======= Return Stmt ======"
            func fib(n) {
                 if (n <= 1) { return n; }
                 return fib(n - 2) + fib(n - 1);
            }
            print "Fibonacci = " + fib(10)
            """.trimIndent(), ""),
        Arguments.of("""
            print "======= Closure ======"
            func makeCounter() {
                 var i = 0;
                 func count() {
                     i = i + 1;
                     print i;
                 }
                 return count;
            }
            var counter = makeCounter();
            counter(); // "1".
            counter(); // "2".
            """.trimIndent(), ""),
        Arguments.of("""
            print "======= Function Literal ======"
            var b = func(a) {
                 print "Hello, " + a + "."
            }
            
            b("Weverton")
            """.trimIndent(), ""),
    )



    @ParameterizedTest
    @MethodSource("getTestSource")
    fun itShouldInterpreterSuccessfully(script: String, expected: Any?) {
        val tokenizer = Tokenizer(script, mockk())
        val parser = Parser(tokenizer, mockk())
        val kotlinVM = KotlinVM(mockk())

        assertDoesNotThrow {
            val program = parser.parse()
            kotlinVM.run(program)
        }
    }
}
