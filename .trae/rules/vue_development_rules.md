---
alwaysApply: true


---


You are an expert in Vue.js development, specializing in Vue 3, Vue Router, Element Plus, SCSS, Axios, Vite, and modern frontend development practices.


# Vue Development Rules

## Code Style and Structure

- Write clean, efficient, and well-documented Vue.js code following best practices.

- Use Vue 3 Composition API with `<script setup>` syntax for all new components.

- Structure Vue applications logically: components, views, router, utils, api, assets.

- Use descriptive variable and function names following camelCase convention.

- Keep components focused and single-responsibility.


## Project Structure

- Organize code into feature-based modules for better maintainability.

- Separate concerns: UI components, business logic, API calls, and utility functions.

- Use the following directory structure:
  ```
  src/
  ├── assets/         # Global assets (images, SCSS, fonts)
  ├── components/     # Reusable components
  ├── views/          # Page components
  ├── router/         # Route configurations
  ├── store/          # State management (Pinia)
  ├── utils/          # Utility functions
  ├── api/            # API request handlers
  ├── App.vue         # Root component
  └── main.js         # Application entry
  ```


## Naming Conventions

- **Component files**: Use PascalCase (e.g., `Login.vue`, `StudentPortraitList.vue`)

- **View files**: Use PascalCase (e.g., `Dashboard.vue`, `UserManagement.vue`)

- **Utility files**: Use kebab-case (e.g., `date-utils.js`, `api-client.js`)

- **SCSS files**: Use kebab-case with underscore prefix for partials (e.g., `_variables.scss`, `_base.scss`)

- **Variables**: Use camelCase (e.g., `userName`, `isLogin`)

- **Constants**: Use UPPER_SNAKE_CASE (e.g., `API_BASE_URL`, `MAX_RETRY_COUNT`)

- **Functions**: Use camelCase with verb prefix (e.g., `getUserInfo()`, `handleLogin()`)


## Component Development

- Use Vue 3 Composition API with `<script setup>` syntax

- Structure components with template, script, and style sections in that order

- Use `<style scoped>` for component-specific styles

- Use SCSS (`lang="scss"`) for styling to leverage variables and nesting

- Avoid deep component nesting (max 3-4 levels)

- Use props for data input and emit events for data output

- Implement proper error handling and loading states


## Template Guidelines

- Use self-closing tags for components without content

- Use v-if/v-else-if/v-else for conditional rendering (avoid multiple v-if)

- Use v-for with key attribute for list rendering

- Limit attributes per line to 3 for better readability

- Use shorthand syntax when appropriate (e.g., `:src`, `@click`)

- Avoid complex expressions in templates; use computed properties instead


## Script Guidelines

- Import order: external dependencies → internal components → utils → API

- Define reactive data using ref() and reactive()

- Use computed properties for derived data

- Use watch() for reactive data changes with side effects

- Use lifecycle hooks appropriately (onMounted, onUnmounted, etc.)

- Avoid direct DOM manipulation; use Vue's reactivity system instead

- Keep component logic focused and extract complex logic to utility functions


## SCSS/Style Guidelines

- Use BEM naming convention for CSS classes (e.g., `block__element--modifier`)

- Define global variables in `_variables.scss`

- Use nesting appropriately to reflect component structure

- Avoid !important declarations

- Use relative units (rem, em, vh, vw) instead of absolute pixels

- Implement responsive design using media queries

- Leverage SCSS features: variables, mixins, functions, and nesting


## Vue Router

- Use Vue Router 4 for routing

- Implement lazy loading for route components

- Use nested routes for complex page structures

- Define route names using camelCase

- Implement navigation guards for authentication and authorization

- Use params for dynamic routes and query for optional parameters


## API Requests

- Use Axios for HTTP requests

- Create API modules by feature (e.g., user.js, student.js)

- Implement request and response interceptors for common functionality

- Handle API errors consistently

- Use async/await for better readability

- Implement request cancellation for long-running requests


## State Management

- Use Pinia for state management (Vue 3 recommended)

- Structure stores by feature

- Use composition API syntax for store definition

- Keep state minimal and focused

- Use actions for asynchronous operations

- Use getters for derived state


## Performance Optimization

- Use v-memo to avoid unnecessary re-renders

- Implement virtual scrolling for large lists

- Lazy load components and images

- Optimize asset sizes (compress images, minify CSS/JS)

- Use code splitting to reduce initial bundle size

- Avoid unnecessary reactive data


## Element Plus

- Use Element Plus as the UI component library

- Import components on-demand to reduce bundle size

- Customize theme variables using SCSS

- Follow Element Plus component usage guidelines

- Use ElMessage, ElNotification, and ElMessageBox for user feedback


## Testing

- Write unit tests for components using Vitest

- Test critical functionality and edge cases

- Use Vue Testing Library for component testing

- Implement end-to-end tests using Cypress

- Test both positive and negative scenarios


## Build and Deployment

- Use Vite for development and production builds

- Configure environment variables for different environments

- Implement proper build optimization (tree shaking, code splitting)

- Use Git for version control with proper branching strategy

- Implement CI/CD pipeline for automated testing and deployment


## Development Tools

- Use VS Code as the primary editor

- Install recommended extensions: ESLint, Prettier, Volar, SCSS IntelliSense, GitLens

- Configure editor to follow project code style rules

- Use browser developer tools for debugging

- Implement proper logging for debugging purposes


## Code Quality

- Use ESLint for code linting

- Use Prettier for code formatting

- Follow the project's .eslintrc.js configuration

- Run lint and format checks before committing code

- Write meaningful commit messages following Conventional Commits


## Git Workflow

- Use main branch as the production branch

- Use develop branch for active development

- Create feature branches for new features (feature/feature-name)

- Create fix branches for bug fixes (fix/bug-name)

- Review code before merging to develop or main

- Keep commits small and focused


## Security

- Implement proper authentication and authorization

- Sanitize user input to prevent XSS attacks

- Use HTTPS in production

- Secure API endpoints with proper authentication

- Implement CSRF protection

- Avoid exposing sensitive information in client-side code


By following these rules, you will create a consistent, maintainable, and high-quality Vue application for both admin and app platforms.