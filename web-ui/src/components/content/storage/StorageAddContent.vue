<template>
  <div>
    <form
      novalidate
      @submit.prevent="validateStorage"
    >
      <md-card>
        <md-card-content>
          <md-field :class="getValidationClass('name')">
            <label>Name</label>
            <md-input
              v-model="name"
              name="name"
              :disabled="sending"
              :maxlength="fieldsLength.STORAGE_NAME"
            />
            <span
              class="md-error"
              v-if="!$v.name.required"
            >
              {{errors.FIELD_IS_REQUIRED}}
            </span>
            <span
              class="md-error"
              v-else-if="!$v.name.maxLength"
            >
              {{errors.MAX_LENGTH(fieldsLength.STORAGE_NAME)}}
            </span>
          </md-field>

          <md-autocomplete
            id="address"
            :class="getValidationClass('address')"
            v-model="address"
            :md-options="addresses"
            :disabled="sending"
            :maxlength="fieldsLength.STORAGE_ADDRESS"
            md-dense
            @input.native="findPlaces"
          >
            <label>Address</label>
            <span
              class="md-error"
              v-if="!$v.address.required"
            >
              {{errors.FIELD_IS_REQUIRED}}
            </span>
            <span
              class="md-error"
              v-else-if="!$v.address.maxLength"
            >
              {{errors.MAX_LENGTH(fieldsLength.STORAGE_ADDRESS)}}
            </span>
            <span
              class="md-error"
              v-else-if="!addressValid"
            >
              {{errors.INVALID_ADDRESS}}
            </span>
          </md-autocomplete>
        </md-card-content>

        <md-progress-bar md-mode="indeterminate" v-if="sending"/>

        <md-card-actions>
          <md-button
            class="md-primary"
            :disabled="sending"
            to="/storages"
          >
            Back
          </md-button>

          <md-button
            type="submit"
            class="md-primary"
            :disabled="sending"
          >
            Save
          </md-button>
        </md-card-actions>

        <md-chip
          class="md-accent md-layout md-alignment-center"
          v-if="hasError"
        >
          {{errorMessage}}
        </md-chip>

        <div id="map"></div>
      </md-card>

      <md-snackbar
        :md-active.sync="storageSaved"
      >
        {{messages.STORAGE_SAVED}}
      </md-snackbar>
    </form>
  </div>
</template>

<script>
  import {required, maxLength} from 'vuelidate/lib/validators'

  import {Errors} from '../../../constants/errors'
  import {FieldsLength} from '../../../constants/fieldsLength'
  import {Messages} from '../../../constants/messages'
  import {Url} from '../../../constants/url'

  export default {
    name: 'StorageAddContent',

    data: () => ({
      errors: Errors,
      fieldsLength: FieldsLength,
      messages: Messages,

      name: null,
      address: null,
      addresses: [],

      storageSaved: false,
      sending: false,
      errorMessage: null,
      hasError: false,

      service: null,
      map: null,
      addressValid: true
    }),

    validations: {
      name: {
        required,
        maxLength: maxLength(FieldsLength.STORAGE_NAME)
      },
      address: {
        required,
        maxLength: maxLength(FieldsLength.STORAGE_ADDRESS)
      }
    },

    methods: {
      getValidationClass(fieldName) {
        const field = this.$v[fieldName]
        if (field) {
          return {
            'md-invalid': field.$invalid && field.$dirty
          }
        }
      },

      findPlaces() {
        const request = {
          query: this.address,
          fields: ['name', 'geometry']
        }

        /* eslint-disable no-undef */
        this.service.findPlaceFromQuery(request, (results, status) => {
          this.addresses = []
          if (status === google.maps.places.PlacesServiceStatus.OK) {
            this.map.setCenter(results[0].geometry.location)
            this.map.setZoom(16)
            for (let i = 0; i < results.length; i++) {
              this.addresses.push(results[i].name)
            }
          }
        })
        /* eslint-enable no-undef */
      },

      validateStorage() {
        this.$v.$touch()
        if (!this.$v.$invalid) {
          const request = {
            query: this.address,
            fields: ['name']
          }

          /* eslint-disable no-undef */
          this.service.findPlaceFromQuery(request, (results, status) => {
            if (status !== google.maps.places.PlacesServiceStatus.OK || results.length !== 1) {
              this.addressValid = false
              document.getElementById('address').classList.add('md-invalid')
            } else {
              document.getElementById('address').classList.remove('md-invalid')
              this.saveStorage()
              this.addressValid = true
            }
          })
          /* eslint-enable no-undef */
        }
      },

      saveStorage() {
        this.sending = true
        const form = {
          name: this.name,
          address: this.address
        }
        this.$http.post(Url.STORAGE, JSON.stringify(form), {
          headers: {
            Authorization: `Bearer ${localStorage.accessToken}`
          }
        })
          .then(() => {
            this.storageSaved = true
            this.sending = false
            this.clearForm()
            this.hasError = false
          }, response => {
            this.hasError = true
            this.sending = false
            this.errorMessage = response.body.errors[0]
          })
        this.$v.$reset()
      },

      clearForm() {
        this.name = null
        this.address = null
        this.addresses = []
      }
    },

    mounted: function() {
      const userRoles = JSON.parse(localStorage.getItem('roles'))
      if (!userRoles || !userRoles.includes('ADMIN')) {
        this.$router.replace('/')
      }

      /* eslint-disable no-undef */
      const map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 53.540, lng: 27.300},
        zoom: 8
      })
      this.map = map
      this.service = new google.maps.places.PlacesService(map)
      /* eslint-enable no-undef */
    }
  }
</script>

<style scoped>
  #map {
    width: 100%;
    height: 500px;
  }
</style>
