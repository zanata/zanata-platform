/* tslint:disable:max-line-length */

// TypeScript wizardry thanks to jcalz:
// https://stackoverflow.com/a/45486495/14379
// https://gist.github.com/jcalz/381562d282ebaa9b41217d1b31e2c211
export type Lit = string | number | boolean | undefined | null | void | {};

// infers a tuple type for up to twelve values (add more here if you need them)
export function tuple<A extends Lit, B extends Lit, C extends Lit, D extends Lit, E extends Lit, F extends Lit, G extends Lit, H extends Lit, I extends Lit, J extends Lit, K extends Lit, L extends Lit>(a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K, l: L): [A, B, C, D, E, F, G, H, I, J, K, L];
export function tuple<A extends Lit, B extends Lit, C extends Lit, D extends Lit, E extends Lit, F extends Lit, G extends Lit, H extends Lit, I extends Lit, J extends Lit, K extends Lit>(a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J, k: K): [A, B, C, D, E, F, G, H, I, J, K];
export function tuple<A extends Lit, B extends Lit, C extends Lit, D extends Lit, E extends Lit, F extends Lit, G extends Lit, H extends Lit, I extends Lit, J extends Lit>(a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J): [A, B, C, D, E, F, G, H, I, J];
export function tuple<A extends Lit, B extends Lit, C extends Lit, D extends Lit, E extends Lit, F extends Lit, G extends Lit, H extends Lit, I extends Lit>(a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I): [A, B, C, D, E, F, G, H, I];
export function tuple<A extends Lit, B extends Lit, C extends Lit, D extends Lit, E extends Lit, F extends Lit, G extends Lit, H extends Lit>(a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H): [A, B, C, D, E, F, G, H];
export function tuple<A extends Lit, B extends Lit, C extends Lit, D extends Lit, E extends Lit, F extends Lit, G extends Lit>(a: A, b: B, c: C, d: D, e: E, f: F, g: G): [A, B, C, D, E, F, G];
export function tuple<A extends Lit, B extends Lit, C extends Lit, D extends Lit, E extends Lit, F extends Lit>(a: A, b: B, c: C, d: D, e: E, f: F): [A, B, C, D, E, F];
export function tuple<A extends Lit, B extends Lit, C extends Lit, D extends Lit, E extends Lit>(a: A, b: B, c: C, d: D, e: E): [A, B, C, D, E];
export function tuple<A extends Lit, B extends Lit, C extends Lit, D extends Lit>(a: A, b: B, c: C, d: D): [A, B, C, D];
export function tuple<A extends Lit, B extends Lit, C extends Lit>(a: A, b: B, c: C): [A, B, C];
export function tuple<A extends Lit, B extends Lit>(a: A, b: B): [A, B];
export function tuple<A extends Lit>(a: A): [A];
export function tuple(...args: any[]): any[] {
  return args;
}
