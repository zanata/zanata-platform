// https://github.com/mosen/commandment/blob/ffa5c089a70eb6c3aff7fcaeaed4e0346a8506d5/ui/src/typings/redux-api-middleware.d.ts

declare module "redux-api-middleware" {
    import {Middleware} from "redux";

    /**
     * Symbol key that carries API call info interpreted by this Redux middleware.
     *
     * @constant {string}
     * @access public
     * @default
     */
    export const RSAA: string;
    export const CALL_API: string;

    //// ERRORS

    /**
     * Error class for an RSAA that does not conform to the RSAA definition
     *
     * @class InvalidRSAA
     * @access public
     * @param {array} validationErrors - an array of validation errors
     */
    export class InvalidRSAA {
        constructor(validationErrors: Array<string>);

        name: string;
        message: string;
        validationErrors: Array<string>;
    }

    /**
     * Error class for a custom `payload` or `meta` function throwing
     *
     * @class InternalError
     * @access public
     * @param {string} message - the error message
     */
    export class InternalError {
        constructor(message: string);

        name: string;
        message: string;
    }

    /**
     * Error class for an error raised trying to make an API call
     *
     * @class RequestError
     * @access public
     * @param {string} message - the error message
     */
    export class RequestError {
        constructor(message: string);

        name: string;
        message: string;
    }

    /**
     * Error class for an API response outside the 200 range
     *
     * @class ApiError
     * @access public
     * @param {number} status - the status code of the API response
     * @param {string} statusText - the status text of the API response
     * @param {object} response - the parsed JSON response of the API server if the
     *  'Content-Type' header signals a JSON response
     */
    export class ApiError {
        constructor(status: number, statusText: string, response: any);

        name: string;
        message: string;
        status: number;
        statusText: string;
        response?: any;
    }

    //// VALIDATION

    /**
     * Is the given action a plain JavaScript object with a [RSAA] property?
     */
    export function isRSAA(action: object): action is RSAAction<any, any, any>;


    export interface TypeDescriptor<TSymbol> {
        type: string | TSymbol;
        payload: any;
        meta: any;
    }

    /**
     * Is the given object a valid type descriptor?
     */
    export function isValidTypeDescriptor(obj: object): obj is TypeDescriptor<any>;

    /**
     * Checks an action against the RSAA definition, returning a (possibly empty)
     * array of validation errors.
     */
    function validateRSAA(action: object): Array<string>;

    /**
     * Is the given action a valid RSAA?
     */
    function isValidRSAA(action: object): boolean;

    //// MIDDLEWARE

    /**
     * A Redux middleware that processes RSAA actions.
     */
    export const apiMiddleware: Middleware;

    //// UTIL

    /**
     * Extract JSON body from a server response
     */
    export function getJSON(res: Response): PromiseLike<any>|undefined;

    export type RSAActionTypeTuple = [string|symbol, string|symbol, string|symbol];

    /**
     * Blow up string or symbol types into full-fledged type descriptors,
     *   and add defaults
     */
    export function normalizeTypeDescriptors(types: RSAActionTypeTuple): RSAActionTypeTuple;



    export type HTTPVerb = 'GET' | 'HEAD' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'OPTIONS';

    export interface RSAAction<R, S, F> {
        [propName: string]: { // Symbol as object key seems impossible
            endpoint: string;  // or function
            method: HTTPVerb;
            body?: any;
            headers?: { [propName: string]: string }; // or function
            credentials?: 'omit' | 'same-origin' | 'include';
            bailout?: boolean; // or function
            types: [R, S, F];
        }
    }

    // module "redux" {
    //     export interface Dispatch<S> {
    //         <R, S, F>(rsaa: RSAAction<R, S, F>): void;
    //     }
    // }
}
