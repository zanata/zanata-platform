declare function exit(result?: number): never
declare const Java: any
declare const arguments: string[]

declare namespace java {
  namespace lang {
    namespace System {
      const out: PrintStream
    }
  }
}

declare interface PrintStream {
  println(s: string): void
}

declare namespace org {
  namespace jboss {
    namespace as {
      namespace cli {
        namespace scriptsupport {
          interface CLI {
            cmd(command: string): CLI.Result
          }
          namespace CLI {
            function newInstance(): CLI
            interface Result {
              cliCommand: string
              localCommand: boolean
              success: boolean
              request: any
              response: any
            }
          }
        }
      }
    }
  }
}
