// from https://github.com/Microsoft/TypeScript/pull/21316

type FunctionPropertyNames<T> = { [K in keyof T]: T[K] extends Function ? K : never }[keyof T]
type FunctionProperties<T> = Pick<T, FunctionPropertyNames<T>>

type NonFunctionPropertyNames<T> = { [K in keyof T]: T[K] extends Function ? never : K }[keyof T]
type NonFunctionProperties<T> = Pick<T, NonFunctionPropertyNames<T>>

export type DeepReadonly<T> =
  T extends any[] ? DeepReadonlyArray<T[number]> :
  T extends object ? DeepReadonlyObject<T> :
  T

interface DeepReadonlyArray<T> extends ReadonlyArray<DeepReadonly<T>> {}

type DeepReadonlyObject<T> = {
  readonly [P in NonFunctionPropertyNames<T>]: DeepReadonly<T[P]>
}
