module.exports = {
  preset: 'jest-preset-angular',
  setupFilesAfterEnv: ['<rootDir>/setup-jest.ts'],
  moduleNameMapper: {
    '^@/core/(.*)$': '<rootDir>/src/app/core/$1',
    '^@/shared/(.*)$': '<rootDir>/src/app/shared/$1',
    '^@/features/(.*)$': '<rootDir>/src/app/features/$1',
    '^@/environments/(.*)$': '<rootDir>/src/environments/$1',
    '^maplibre-gl$': '<rootDir>/src/__mocks__/maplibre-gl.ts',
  },
  testPathIgnorePatterns: ['<rootDir>/node_modules/', '<rootDir>/e2e/'],
  coverageDirectory: '<rootDir>/coverage',
  collectCoverageFrom: [
    'src/app/**/*.ts',
    '!src/app/**/*.spec.ts',
    '!src/app/**/*.routes.ts',
    '!src/app/app.config.ts',
  ],
  coverageThreshold: {
    global: {
      statements: 90,
      branches: 90,
      functions: 90,
      lines: 90,
    },
  },
};
