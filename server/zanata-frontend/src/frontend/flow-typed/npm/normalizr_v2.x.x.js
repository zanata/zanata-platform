// flow-typed signature: cb1d7de99931f6c329dcbd2f5fdb2236
// flow-typed version: b43dff3e0e/normalizr_v2.x.x/flow_>=v0.15.x

declare class Normalizr$Schema {
  define(nestedSchema: Object): void;
}
type Normalizr$SchemaOrObject = Normalizr$Schema | Object;

declare module 'normalizr' {
  declare class Normalizr {
    normalize(obj: Object | Array<Object>, schema: Normalizr$SchemaOrObject): Object;
    Schema(key: string, options?: Object): Normalizr$Schema;
    arrayOf(schema: Normalizr$SchemaOrObject, options?: Object): Normalizr$Schema;
    valuesOf(schema: Normalizr$SchemaOrObject, options?: Object): Normalizr$Schema;
  }
  declare var exports: Normalizr;
}
