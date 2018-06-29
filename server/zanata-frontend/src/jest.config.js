// @ts-check

/**
 * @type {Partial<jest.DefaultOptions & jest.InitialOptions>}
 */
const config = {
  automock: false,
  collectCoverageFrom: [
    "app/**/*.{js,jsx,ts,tsx}",
    "!**/node_modules/**",
    "!app/**/*.story.{js,jsx,ts,tsx}",
    "!**/*.d.ts"
  ],
  coverageReporters: [
    "cobertura",
    "html",
    "lcov",
    "text"
  ],
  moduleNameMapper: {
    "\\.(css|less)$": "<rootDir>/__mocks__/cssMock.js"
  },
  transform: {
    "\\.(jsx?|tsx?)$": "ts-jest"
  },
  testRegex: "(/__tests__/.*|(\\.|/)(test|spec))\\.[jt]sx?$",
  testPathIgnorePatterns: [
    "<rootDir>/dist/",
    "<rootDir>/build/",
    "<rootDir>/node_modules/",
    "<rootDir>/__mocks__"
  ],
  unmockedModulePathPatterns: [
    "/node_modules",
    "/app"
  ],
  moduleFileExtensions: [
    "js",
    "jsx",
    "json",
    "ts",
    "tsx"
  ]
}

module.exports = config
