module.exports = {
  collectCoverageFrom: [
    'app/**/*.{js,jsx}',
    '!**/node_modules/**',
    '!app/**/*.story.js'
  ],
  coverageReporters: [
    'cobertura',
    'html',
    'lcov',
    'text'
  ],
  moduleNameMapper: {
    // FIXME this seems not to be applied properly
    '\\.(css|less)$': '<rootDir>/__mocks__/cssMock.js'
  },
  transform: {
    '.*': './node_modules/babel-jest'
  },
  testPathIgnorePatterns: [
    '/node_modules/',
    '/__tests__/mock'
  ],
  unmockedModulePathPatterns: [
    '/node_modules',
    '/app'
  ],
  moduleFileExtensions: [
    'js',
    'jsx',
    'json'
  ]
}
