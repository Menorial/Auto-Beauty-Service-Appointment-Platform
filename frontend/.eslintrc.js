module.exports = {
  root: true,
  env: {
    node: true,
    'vue/setup-compiler-macros': true
  },
  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended',
    '@vue/prettier'
  ],
  parserOptions: {
    ecmaVersion: 2020
  },
  rules: {
    'no-console': 'off',
    'no-debugger': 'off',
    'vue/multi-word-component-names': 'off',
    'prettier/prettier': 'off',
    'no-unused-vars': 'off',
    'no-useless-catch': 'off',
    'no-case-declarations': 'off',
    'vue/no-ref-as-operand': 'off'
  }
}
